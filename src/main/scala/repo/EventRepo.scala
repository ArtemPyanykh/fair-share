package repo

import cqrs.EventData

import scalaz.\/

trait EventRepo[K, C, M] {
  def getAll(id: K): Throwable \/ (Option[EventData[K, C]], Vector[EventData[K, M]])

  def storeAll(id: K, c: EventData[K, C], ms: Vector[EventData[K, M]]): Throwable \/ Unit

  def storeAll(id: K, ms: Vector[EventData[K, M]]): Throwable \/ Unit
}
