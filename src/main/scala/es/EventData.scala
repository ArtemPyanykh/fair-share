package es

import java.time.LocalDateTime

case class EventData[E](version: Version, event: E, createdAt: LocalDateTime)

object EventData {
  def now[E](version: Version, event: E): EventData[E] = EventData(version, event, LocalDateTime.now())
}
