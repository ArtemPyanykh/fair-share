import playground._
import scala.collection.mutable
import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.Sink
import scalaz.concurrent.Task
import scalaz.stream.{sink, Process, async}
val store = mutable.ArrayBuffer[StreamElement[ProjectEvent]]()
val memoryProcessReader = new InMemoryProcessStreamReader[ProjectEvent](store)

