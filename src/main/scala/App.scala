import eventsourcing.journals.{ InMemoryJournal, PollingJournalReader }
import org.http4s.server.blaze.BlazeBuilder
import project._

object App {
  def main(args: Array[String]): Unit = {
    val port = 8080

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

    println("Server started. Type Ctrl-C to-to terminate...")

    server.awaitShutdown()
  }
}
