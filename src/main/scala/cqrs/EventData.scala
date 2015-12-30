package cqrs

import java.time.LocalDateTime

case class EventData[K, E](id: K, version: Version, event: E, createdAt: LocalDateTime)

object EventData {
  def now[K, E](id: K, version: Version, event: E): EventData[K, E] = EventData(id, version, event, LocalDateTime.now())
}
