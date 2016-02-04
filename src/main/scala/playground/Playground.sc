import playground._
import scala.collection.mutable
import java.util.UUID

val p = new ProjectEventProcessor

val storage = mutable.ArrayBuffer[StreamElement[ProjectEvent]]()

val streamReader = new InMemoryStreamReader[ProjectEvent](storage)

val streamWriter = new InMemoryStreamWriter[ProjectEvent](storage)

val eventStore = EventStore(streamReader, streamWriter)

val pcs = new ProjectCommandService

val pid = Project.Id(UUID.randomUUID())

val createAction = pcs.create(pid, "Hello", "World")
createAction(eventStore)

val changeNameAction = pcs.changeName(pid, "New Name", StreamRevision(1))
changeNameAction(eventStore)

changeNameAction(eventStore)

createAction(eventStore)

val changeNameNonExistent = pcs.changeName(Project.Id(UUID.randomUUID()), "Non existent", StreamRevision(1))

changeNameNonExistent(eventStore)

val pvs = new ProjectViewService(streamReader)

pvs.list

pcs.create(Project.Id(UUID.randomUUID()), "Project 2", "Fuck yeah!")(eventStore)

pvs.list

