import java.util.UUID
import playground.ProjectEvent._
import playground._
import scala.collection.mutable.ArrayBuffer

val buffer = new ArrayBuffer[Fact[ProjectEvent]]()
val store = new InMemoryJournal[ProjectEvent](buffer)
val key = FactSubject("test")
val projectId = Project.Id(UUID.randomUUID())
val eventProcessor = new ProjectEventProcessor

store.write(key, SubjectRevision.initial, Created(projectId, "awesomeProject", "motto")).run
store.write(key, SubjectRevision(2), NameModified("new name")).run

store.readSubject(
  FactSubject("test"),
  SubjectRevision.initial
).fold[Snapshot[Project]](Void)((s, e) => eventProcessor.processOne(e.data)(s))
.runLastOr(Void).run

