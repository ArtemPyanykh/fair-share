package fairshare.backend

import com.typesafe.scalalogging.LazyLogging
import fairshare.backend.eventsourcing.journals.{InMemoryJournal, PollingJournalReader}
import fairshare.backend.project.{ProjectCommandHandler, ProjectController, ProjectEvent, ProjectQueryHandler}
import org.http4s.server.blaze.BlazeBuilder

object App extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val port = Globals.serverCfg.getInt("serverInfo.port")

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
