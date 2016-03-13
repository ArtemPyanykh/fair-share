package fairshare.backend.project

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import fairshare.backend.eventsourcing.Fact
import fairshare.backend.eventsourcing.journals.PollingJournalReader

import scalaz.concurrent.Task
import scalaz.stream.sink
import scala.collection.JavaConverters._

class ProjectQueryHandler(reader: PollingJournalReader[ProjectEvent]) {
  val projectList = new ConcurrentHashMap[ProjectId, String]()

  private def updateProjectList(fact: Fact[ProjectEvent]): Unit = {
    val projectId = ProjectId(UUID.fromString(fact.subject.key))

    val projectName = fact.event match {
      case ProjectEvent.Created(_, name) => Some(name)
      case ProjectEvent.NameModified(name) => Some(name)
      case _ => None
    }

    for {
      name <- projectName
    } projectList.put(projectId, name)
  }

  val listUpdater = reader.readUpdates.to(
    sink.lift[Task, Fact[ProjectEvent]] {
      fact =>
        Task.delay {
          updateProjectList(fact)
        }
    }
  )

  listUpdater.run.runAsync(_ => ())

  def getAll: Map[ProjectId, String] = projectList.asScala.toMap

  def getById(projectId: ProjectId): Option[String] = projectList.asScala.get(projectId)
}
