package project

import cqrs.{ChangeHandler, CreationHandler}
import util.ids._

case class ProjectCreated(id: ProjectId, name: String)

sealed trait ProjectChanged

case class ProjectNameChanged(name: String) extends ProjectChanged

case class ProjectStatusChanged(status: ProjectStatus) extends ProjectChanged

private[project] trait ProjectEventHandlers {
  implicit val createHandler: CreationHandler[Project, ProjectCreated] = CreationHandler { event =>
    Project(id = event.id, name = event.name, ProjectStatus.Open)
  }

  implicit val changeHandler: ChangeHandler[Project, ProjectChanged] = ChangeHandler { (project, event) =>
    event match {
      case ProjectNameChanged(name) => project.copy(name = name)
      case ProjectStatusChanged(status) => project.copy(status = status)
    }
  }
}

