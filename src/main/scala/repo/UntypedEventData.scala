package repo

import java.time.LocalDateTime

import cqrs._
import cqrs.typeclass.StringCodec.ops._
import cqrs.typeclass.Tagged.ops._
import cqrs.typeclass.{ StringCodec, Tagged }

case class UntypedEventData(
  aggregateTag: String,
  aggregateId: String,
  aggregateVersion: Int,
  eventTag: String,
  eventData: String,
  createdAt: LocalDateTime
)

object UntypedEventData {
  def fromEventData[K: StringCodec: Tagged, E: StringCodec: Tagged](id: K, data: EventData[E]): UntypedEventData = {
    val aggregateTag = id.tag
    val aggregateId = id.encode

    val eventTag = data.event.tag
    val eventData = data.event.encode

    UntypedEventData(
      aggregateTag,
      aggregateId,
      data.version.value,
      eventTag,
      eventData,
      data.createdAt
    )
  }

  def toEventData[K: StringCodec, E: StringCodec](raw: UntypedEventData): (K, EventData[E]) = {
    val keyCodec = implicitly[StringCodec[K]]
    val eventCoded = implicitly[StringCodec[E]]

    val id = keyCodec.decode(raw.aggregateId)
    val event = eventCoded.decode(raw.eventData)
    val version = Version(raw.aggregateVersion)

    (id, EventData(version, event, raw.createdAt))
  }
}
