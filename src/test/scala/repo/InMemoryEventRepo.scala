package repo

import cqrs.EventData
import util.Id

import scala.collection.mutable
import scalaz.\/
import scalaz.syntax.either._

class InMemoryEventRepo[A, C, M] extends EventRepo[A, C, M] {
  val inMemoryCStore = mutable.Map[Id[A], EventData[C]]()
  val inMemoryMStore = mutable.Map[Id[A], Vector[EventData[M]]]()

  def getAll(id: Id[A]): (Throwable \/ (Option[EventData[C]], Vector[EventData[M]])) = {
    val cEvent = inMemoryCStore.get(id)
    val mEvents = inMemoryMStore.getOrElse(id, Vector.empty)

    (cEvent, mEvents).right
  }

  def storeAll(id: Id[A], c: EventData[C], ms: Vector[EventData[M]]): (Throwable \/ Unit) = inMemoryCStore.synchronized {
    inMemoryCStore += (id -> c)
    storeAll(id, ms)
    ().right
  }

  def storeAll(id: Id[A], ms: Vector[EventData[M]]): (Throwable \/ Unit) = inMemoryMStore.synchronized {
    val events = inMemoryMStore.getOrElse(id, Vector.empty) ++ ms
    inMemoryMStore += (id -> events)
    ().right
  }
}
