package project

import cqrs.typeclass.{ Tagged, EventM, EventC }
import util.ids._

case class ProjectCreated(id: ProjectId, name: String)

object ProjectCreated {
  implicit val eventC: EventC[Project, ProjectCreated] = EventC {
    case ProjectCreated(id, name) => Project(id = id, name = name, ProjectStatus.Open)
  }

  implicit val tag: Tagged[ProjectCreated] = new Tagged[ProjectCreated] {
    def tag(a: ProjectCreated): String = "project-created"
  }
}

sealed trait ProjectModified

case class ProjectNameModified(name: String) extends ProjectModified

case class ProjectStatusModified(status: ProjectStatus) extends ProjectModified

object ProjectModified {
  implicit val eventM: EventM[Project, ProjectModified] = EventM { model =>
    {
      case ProjectNameModified(name) => model.copy(name = name)
      case ProjectStatusModified(status) => model.copy(status = status)
    }
  }

  implicit val tag: Tagged[ProjectModified] = new Tagged[ProjectModified] {
    def tag(a: ProjectModified): String = a match {
      case x @ ProjectNameModified(_) => "ProjectNameModified"
      case x @ ProjectStatusModified(_) => "ProjectStatusModified"
    }
  }
}
