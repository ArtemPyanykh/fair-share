package project

import eventsourcing._

import scalaz.\/
import scalaz.concurrent.Task
import scalaz.syntax.either._

class ProjectCommandHandler(journal: Journal[ProjectEvent]) {
  type Result[T] = Task[String \/ T]

  import ProjectEvent._

  def create(id: ProjectId, name: String, motto: String): Result[Fact[ProjectEvent]] = {
    journal.write(Subject(id.uuid.toString), Revision.initial, Created(id, name)).map {
      case WriteSuccess(el) => el.right
      case WriteFailure(cause) => cause.left
    }
  }

  def changeName(
    id: ProjectId,
    name: String,
    revision: Revision
  ): Result[Fact[ProjectEvent]] = {
    journal.readSnapshot[Project, ProjectId](id).flatMap {
      case Healthy(project, _) =>
        journal.writeI(id, revision, NameModified(name)).map {
          case WriteSuccess(el) => el.right
          case WriteFailure(cause) => cause.left
        }

      case _ => Task.now {
        s"Project with id ${id.uuid.toString} doesn't exist".left
      }
    }
  }

}
