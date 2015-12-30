package project

import cqrs.{ ChangeEvent, CreationEvent, ChangeHandler, CreationHandler }
import util.ids._

case class ProjectCreated(id: ProjectId, name: String) extends CreationEvent[Project] {
  def run: Project = Project(id = id, name = name, status = ProjectStatus.Open)
}

sealed trait ProjectChanged extends ChangeEvent[Project] {
  def run(model: Project): Project = this match {
    case ProjectNameChanged(name) => model.copy(name = name)
    case ProjectStatusChanged(status) => model.copy(status = status)
  }
}

case class ProjectNameChanged(name: String) extends ProjectChanged

case class ProjectStatusChanged(status: ProjectStatus) extends ProjectChanged
