package playground

import java.util.UUID
import java.util.concurrent.{ ConcurrentHashMap, Executors }

import command.project.{ ProjectCommandService, ProjectEvent, Project }
import eventsourcing.journals.{ PollingJournalReader, PollingJournalReader$, InMemoryJournal }
import eventsourcing.Entry
import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration._
import scalaz.concurrent.Task
import scalaz.stream.sink

object App {

  import org.http4s.dsl._

  val executor = Executors.newScheduledThreadPool(1)
  val updateFrequency = 100.millis
  val journal = new InMemoryJournal[ProjectEvent]
  val reader = new PollingJournalReader(journal, updateFrequency)(executor)

  val projectList = new ConcurrentHashMap[Project.Id, String]()

  def updateProjectList(fact: Entry[ProjectEvent]): Unit = {
    val projectId = Project.Id(UUID.fromString(fact.subject.key))

    val projectName = fact.event match {
      case ProjectEvent.Created(_, name, _) => Some(name)
      case ProjectEvent.NameModified(name) => Some(name)
      case _ => None
    }

    for {
      name <- projectName
    } projectList.put(projectId, name)
  }

  val listUpdater = reader.readUpdates.to(
    sink.lift[Task, Entry[ProjectEvent]] {
      fact =>
        Task.delay {
          updateProjectList(fact)
        }
    }
  )

  val projectService = new ProjectCommandService

  val example = HttpService {
    case req @ POST -> Root / "create" =>
      val nameOpt = req.params.get("name")
      val mottoOpt = req.params.get("motto")
      val id = Project.Id(UUID.randomUUID())

      val r = for {
        name <- nameOpt
        motto <- mottoOpt
      } yield {
        projectService.create(id, name, motto).run(journal).fold(
          error => InternalServerError(error),
          fact => Ok(s"Project ${fact.subject.key} was successfully created")
        ).run
      }

      r match {
        case None => BadRequest("Some parameters are missing")
        case Some(resp) => Task.delay { resp }
      }

    case req @ GET -> Root / "index" =>
      val projects = projectList.asScala.map {
        case (projectId, name) => s"${projectId.uuid} -> $name"
      }.toSeq

      Ok(projects.reduce(_ + ", " + _))
  }

  def main(args: Array[String]): Unit = {
    val port = 8080

    listUpdater.run.runAsync(_ => ())
    reader.listenUpdates.run.runAsync(_ => ())

    BlazeBuilder.bindHttp(port)
      .mountService(example)
      .run.awaitShutdown()
  }

}
