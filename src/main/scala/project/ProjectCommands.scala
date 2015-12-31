package project

import cqrs._
import util.ids._
import util.types._

import scalaz.syntax.either._

trait ProjectCommands[ProjectAggregate] {
  def create(id: ProjectId, name: String): CreateValidS[ProjectAggregate]

  def modifyName(name: String): ChangeValidS[ProjectAggregate]

  def modifyStatus(status: ProjectStatus): ChangeValidS[ProjectAggregate]

  def checkAgainstVersion(version: Version): ChangeValidS[ProjectAggregate]
}

object ProjectCommandsInterpreter {
  type ProjectAggregate = Aggregate[Project, ProjectCreated, ProjectModified]

  import cqrs.AggregateCommand
  import validations._

  val projectCreate = AggregateCommand[Project, ProjectCreated, ProjectModified].create _
  val projectChange = AggregateCommand[Project, ProjectCreated, ProjectModified].modify _

  object EventSourcedProjectCommands extends ProjectCommands[ProjectAggregate] {
    def create(id: ProjectId, name: String): CreateValidS[ProjectAggregate] = CreateValidS[ProjectAggregate] { _: Unit =>
      for {
        name <- validateName(name)
      } yield projectCreate(ProjectCreated(id, name))
    }

    def modifyStatus(status: ProjectStatus): ChangeValidS[ProjectAggregate] = ChangeValidS[ProjectAggregate] { pa =>
      val project = pa.model
      for {
        status <- validateStatus(project.status, status)
      } yield projectChange(pa, ProjectStatusModified(status))
    }

    def checkAgainstVersion(version: Version): ChangeValidS[ProjectAggregate] = ChangeValidS[ProjectAggregate] { pa =>
      if (version == pa.version) pa.right else s"Project version mismatch. Expected ${pa.version}, actual $version".left
    }

    def modifyName(name: String): ChangeValidS[ProjectAggregate] = ChangeValidS[ProjectAggregate] { pa =>
      for {
        name <- validateName(name)
      } yield projectChange(pa, ProjectNameModified(name))
    }
  }

  object validations { // scalastyle:ignore
    def validateName(name: String): ValidS[String] = if (name.length > 0) {
      name.right
    } else {
      "Name shouldn't be empty".left
    }

    def validateStatus(oldStatus: ProjectStatus, newStatus: ProjectStatus): ValidS[ProjectStatus] = {
      import ProjectStatus._
      val validTransitions = List(Open -> ProjectStatus.Finished, Open -> Removed, Removed -> Open, Finished -> Open)

      if (validTransitions.contains(oldStatus -> newStatus)) {
        newStatus.right
      } else {
        s"Cannot transition to specified status".left
      }
    }
  }

}

