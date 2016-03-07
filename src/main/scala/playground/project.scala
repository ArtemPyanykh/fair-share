package playground

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import scalaz.{ \/, Reader }
import scalaz.syntax.either._
import scala.collection.mutable

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
  def create(id: Project.Id, name: String, motto: String): Reader[EventStore[ProjectEvent], Result[StreamElement[ProjectEvent]]] = Reader { store =>
    val writeResult = store.writer.write(StreamKey(id.uuid.toString), StreamRevision.initial, Created(id, name, motto))
    writeResult match {
      case WriteSuccess(el) => el.right
      case WriteFailure(el) => s"Project with such id already exists: ${el.key}".left
    }
  }

  def changeName(
    id: Project.Id,
    name: String,
    revision: StreamRevision
  ): Reader[EventStore[ProjectEvent], Result[StreamElement[ProjectEvent]]] = Reader { store =>
    val events = store.reader.readKey(StreamKey(id.uuid.toString), StreamRevision.initial)
    val processor = new ProjectEventProcessor
    if (isNameValid(name)) {
      val snapshot = processor.process(Void, events.map(_.data).toList)
      snapshot match {
        case Healthy(project) =>
          val writeResult = store.writer.write(
            StreamKey(id.uuid.toString), revision, NameModified(name)
          )
          writeResult match {
            case WriteSuccess(el) => el.right
            case WriteFailure(el) =>
              s"Trying to act on an outdated data. Revision ${revision} was created at ${el.createdAt}".left
          }
        case _ => s"Project with id ${id.uuid.toString} doesn't exist".left
      }
    } else {
      "Name is empty".left
    }
  }

  def validateName(name: String): Result[String] = if (!name.isEmpty) name.right else "Name is empty".left
}

case class ProjectView(id: Project.Id, name: String)

class ProjectViewService(reader: StreamReader[ProjectEvent]) {
  val projectRegistry = new AtomicReference[Map[Project.Id, ProjectView]](Map.empty)

  def list: List[(Project.Id, ProjectView)] = {
    buildRegistry()
    projectRegistry.get().toList
  }

  private def buildRegistry(): Unit = {
    import ProjectEvent._

    val streamEls = reader.readAll(SequenceNumber.initial)
    val newRegistry = mutable.Map[Project.Id, ProjectView]()

    streamEls.foreach { el =>
      val projectId = Project.Id(UUID.fromString(el.key.key))
      val existing = newRegistry.get(projectId)

      existing match {
        case None =>
          el.data match {
            case Created(id, name, _) => newRegistry += (projectId -> ProjectView(id, name))
            case _ => ()
          }
        case Some(projectView) =>
          el.data match {
            case NameModified(newName) => newRegistry.update(projectId, projectView.copy(name = newName))
            case _ => ()
          }
      }
    }

    projectRegistry.set(newRegistry.toMap)
  }
}
