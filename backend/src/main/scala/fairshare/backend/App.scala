package fairshare.backend

import com.typesafe.config._
import com.typesafe.scalalogging.LazyLogging
import eventsourcing.journals.{InMemoryJournal, PollingJournalReader}
import org.http4s.server.blaze.BlazeBuilder
import project.{ProjectCommandHandler, ProjectController, ProjectEvent, ProjectQueryHandler}

object App extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val port = ConfigFactory.load("server").getInt("serverInfo.port")

    val projectJournal = InMemoryJournal[ProjectEvent]
    val projectPollingReader = new PollingJournalReader(projectJournal, Globals.pollingFrequency)(Globals.executor)

    val projectService = new ProjectController(
      new ProjectCommandHandler(projectJournal),
      new ProjectQueryHandler(projectPollingReader)
    )

    projectPollingReader.listenUpdates.run.runAsync(_ => ())

    val server = BlazeBuilder.bindHttp(port)
      .mountService(ProjectController.service(projectService))
      .run

    server.awaitShutdown()
  }
}
