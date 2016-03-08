package playground

import java.util.UUID

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

class ProjectEventProcessor extends EventProcessor[Project, ProjectEvent] {
  import playground.ProjectEvent._

  def processOne(event: ProjectEvent): Snapshot[Project] => Snapshot[Project] = event match {
    case Created(id, name, motto) => attemptCreate(Project(id, name, motto))
    case NameModified(newName) => attemptModify(_.copy(name = newName))
    case MottoModified(newMotto) => attemptModify(_.copy(motto = newMotto))
  }
}

class ProjectCommandService {
  type Result[T] = String \/ T

  import ProjectEvent._
  def create(id: Project.Id, name: String, motto: String): Reader[Journal[ProjectEvent], Result[Fact[ProjectEvent]]] = Reader { store =>
    val writeResult = store.write(FactSubject(id.uuid.toString), SubjectRevision.initial, Created(id, name, motto)).run
    writeResult match {
      case WriteSuccess(el) => el.right
      case WriteFailure(cause) => cause.left
    }
  }

  def changeName(
    id: Project.Id,
    name: String,
    revision: SubjectRevision
  ): Reader[Journal[ProjectEvent], Result[Fact[ProjectEvent]]] = Reader { store =>
    val events = store.readSubject(FactSubject(id.uuid.toString)).runLog.run
    val processor = new ProjectEventProcessor
    val snapshot = processor.process(Void, events.map(_.data).toList)
    snapshot match {
      case Healthy(project) =>
        val writeResult = store.write(
          FactSubject(id.uuid.toString), revision, NameModified(name)
        ).run
        writeResult match {
          case WriteSuccess(el) => el.right
          case WriteFailure(cause) => cause.left
        }
      case _ => s"Project with id ${id.uuid.toString} doesn't exist".left
    }
  }
}

