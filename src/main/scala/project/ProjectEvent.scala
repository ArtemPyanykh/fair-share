package project

import eventsourcing.FactProcessor

sealed trait ProjectEvent

object ProjectEvent {

  case class Created(id: ProjectId, name: String) extends ProjectEvent

  case class NameModified(name: String) extends ProjectEvent

  import FactProcessor._

  implicit val processor: FactProcessor[Project, ProjectEvent] = BasedOnEvents {
    case Created(id, name) => attemptCreate(Project(id, name))
    case NameModified(newName) => attemptModify(_.copy(name = newName))
  }
}
