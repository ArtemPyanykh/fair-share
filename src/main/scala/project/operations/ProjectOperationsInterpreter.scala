package project.operations

import es.Version
import project.ProjectAggregate
import project.events._
import project.model.{ ProjectStatus, Project }

import util.types._
import util.ids._
import scalaz.syntax.either._
import es.AggregateCommand
import project.model.ProjectStatus._

object ProjectOperationsInterpreter {
  import validations._

  val projectCreate = AggregateCommand[Project, ProjectCreated, ProjectModified].create _
  val projectChange = AggregateCommand[Project, ProjectCreated, ProjectModified].modify _

  object ProjectOperations extends ProjectOperations {
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
      if (version == pa.currentVersion) pa.right else s"Project version mismatch. Expected ${pa.currentVersion}, actual $version".left
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
      val validTransitions = List(Open -> ProjectStatus.Finished, Open -> Removed, Removed -> Open, Finished -> Open)

      if (validTransitions.contains(oldStatus -> newStatus)) {
        newStatus.right
      } else {
        s"Cannot transition to specified status".left
      }
    }
  }

}
