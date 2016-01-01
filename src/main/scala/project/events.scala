package project

import cqrs.typeclass._
import util.ids._
import util.types._
import argonaut._
import Argonaut._
import util.json._

case class ProjectCreated(id: ProjectId, name: String)

object ProjectCreated extends ProjectCreatedCodecs {
  implicit val eventC: EventC[Project, ProjectCreated] = EventC {
    case ProjectCreated(id, name) => Project(id = id, name = name, ProjectStatus.Open)
  }

  implicit val tag: Tagged[ProjectCreated] = new Tagged[ProjectCreated] {
    def tag(a: ProjectCreated): String = "ProjectCreated"
  }
}

sealed trait ProjectModified

case class ProjectNameModified(name: String) extends ProjectModified

case class ProjectStatusModified(status: ProjectStatus) extends ProjectModified

object ProjectModified extends ProjectModifiedCodecs {
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
