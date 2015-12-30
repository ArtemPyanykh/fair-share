package project

import cqrs.{EventAggregate, Version}
import util.ids._
import util.types._

import scalaz.syntax.either._

trait ProjectCommands[ProjectAggregate] {
  def create(id: ProjectId, name: String): CreateValidS[ProjectAggregate]

  def changeName(name: String): ChangeValidS[ProjectAggregate]

  def changeStatus(status: ProjectStatus): ChangeValidS[ProjectAggregate]

  def checkAgainstVersion(version: Version): ChangeValidS[ProjectAggregate]
}

object ProjectCommandsInterpreter {
  type ProjectAggregate = EventAggregate[Project, ProjectCreated, ProjectChanged]

  import cqrs.EventSourcedCommands
  import validations._

  val projectCreate = EventSourcedCommands.create[Project, ProjectCreated, ProjectChanged] _
  val projectChange = EventSourcedCommands.change[Project, ProjectCreated, ProjectChanged] _

  object EventSourcedProjectCommands extends ProjectCommands[ProjectAggregate] {
    def create(id: ProjectId, name: String): CreateValidS[ProjectAggregate] = CreateValidS[ProjectAggregate] { _: Unit =>
      for {
        name <- validateName(name)
      } yield projectCreate(ProjectCreated(id, name))
    }

    def changeStatus(status: ProjectStatus): ChangeValidS[ProjectAggregate] = ChangeValidS[ProjectAggregate] { pa =>
      val project = pa.model
      for {
        status <- validateStatus(project.status, status)
      } yield projectChange(pa, ProjectStatusChanged(status))
    }

    def checkAgainstVersion(version: Version): ChangeValidS[ProjectAggregate] = ChangeValidS[ProjectAggregate] { pa =>
      if (version == pa.version) pa.right else s"Project version mismatch. Expected ${pa.version}, actual $version".left
    }

    def changeName(name: String): ChangeValidS[ProjectAggregate] = ChangeValidS[ProjectAggregate] { pa =>
      for {
        name <- validateName(name)
      } yield projectChange(pa, ProjectNameChanged(name))
    }
  }

  object validations {
    def validateName(name: String): ValidS[String] = if (name.length > 0) {
      name.right
    } else {
      "Name shouldn't be empty".left
    }

    def validateStatus(oldStatus: ProjectStatus, newStatus: ProjectStatus): ValidS[ProjectStatus] = {
      import ProjectStatus._
      val validTransitions = List(Open -> ProjectStatus.Finished, Open -> Removed, Removed -> Open, Finished -> Open)
      if (validTransitions.contains(
        oldStatus -> newStatus
      )) newStatus.right
      else s"Cannot transition to specified status".left
    }
  }

}

