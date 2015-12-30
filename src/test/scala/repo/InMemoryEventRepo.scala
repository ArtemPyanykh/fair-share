package repo

import cqrs.EventData

import scala.collection.mutable
import scalaz.\/
import scalaz.syntax.either._

class InMemoryEventRepo[K, C, M] extends EventRepo[K, C, M] {
  val inMemoryCStore = mutable.Map[K, EventData[K, C]]()
  val inMemoryMStore = mutable.Map[K, Vector[EventData[K, M]]]()

  def getAll(id: K): (Throwable \/ (Option[EventData[K, C]], Vector[EventData[K, M]])) = {
    val cEvent = inMemoryCStore.get(id)
    val mEvents = inMemoryMStore.getOrElse(id, Vector.empty)

    (cEvent, mEvents).right
  }

  def storeAll(id: K, c: EventData[K, C], ms: Vector[EventData[K, M]]): (Throwable \/ Unit) = inMemoryCStore.synchronized {
    inMemoryCStore += (id -> c)
    storeAll(id, ms)
    ().right
  }

  def storeAll(id: K, ms: Vector[EventData[K, M]]): (Throwable \/ Unit) = inMemoryMStore.synchronized {
    val events = inMemoryMStore.getOrElse(id, Vector.empty) ++ ms
    inMemoryMStore += (id -> events)
    ().right
  }
}
