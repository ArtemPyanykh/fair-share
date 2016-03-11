package command.project

import java.util.UUID

import eventsourcing._

import scalaz.syntax.either._
import scalaz.{ Reader, \/ }

case class Project(id: Project.Id, name: String, motto: String)

object Project {
  case class Id(uuid: UUID) extends AnyVal
}

sealed trait ProjectEvent

object ProjectEvent {
  case class Created(id: Project.Id, name: String, motto: String) extends ProjectEvent
  case class NameModified(name: String) extends ProjectEvent
  case class MottoModified(motto: String) extends ProjectEvent
}

class ProjectEntryProcessor extends EntryProcessor[Project, ProjectEvent] {
  import ProjectEvent._

  def processEntry: (Entry[ProjectEvent], Snapshot[Project]) => Snapshot[Project] = basedOnEvent {
    case Created(id, name, motto) => attemptCreate(Project(id, name, motto))
    case NameModified(newName) => attemptModify(_.copy(name = newName))
    case MottoModified(newMotto) => attemptModify(_.copy(motto = newMotto))
  }
}

class ProjectCommandService {
  type Result[T] = String \/ T

  import ProjectEvent._
  def create(id: Project.Id, name: String, motto: String): Reader[Journal[ProjectEvent], Result[Entry[ProjectEvent]]] = Reader { store =>
    val writeResult = store.write(Subject(id.uuid.toString), Revision.initial, Created(id, name, motto)).run
    writeResult match {
      case WriteSuccess(el) => el.right
      case WriteFailure(cause) => cause.left
    }
  }

  def changeName(
    id: Project.Id,
    name: String,
    revision: Revision
  ): Reader[Journal[ProjectEvent], Result[Entry[ProjectEvent]]] = Reader { store =>
    val events = store.readSubject(Subject(id.uuid.toString)).runLog.run
    val processor = new ProjectEntryProcessor
    val snapshot = processor.processEntries(Void, events.toList)
    snapshot match {
      case Healthy(project, _) =>
        val writeResult = store.write(
          Subject(id.uuid.toString), revision, NameModified(name)
        ).run
        writeResult match {
          case WriteSuccess(el) => el.right
          case WriteFailure(cause) => cause.left
        }
      case _ => s"Project with id ${id.uuid.toString} doesn't exist".left
    }
  }
}

