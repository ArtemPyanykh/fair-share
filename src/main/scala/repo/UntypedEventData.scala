package repo

import java.time.LocalDateTime

import argonaut.Argonaut._
import argonaut.CodecJson
import es._
import es.typeclass.Tag
import es.typeclass.Tag.ops._
import util.types._

case class UntypedEventData(
  aggregateTag: String,
  aggregateId: String,
  aggregateVersion: Int,
  eventData: String,
  createdAt: LocalDateTime
)

object UntypedEventData {
  def fromEventData[K: CodecJson: Tag, E: CodecJson](id: K, data: EventData[E]): UntypedEventData = {
    val aggregateTag = id.tag
    val aggregateId = id.asJson.nospaces

    val eventData = data.event.asJson.nospaces

    UntypedEventData(
      aggregateTag,
      aggregateId,
      data.version.value,
      eventData,
      data.createdAt
    )
  }

  def toEventData[K: CodecJson, E: CodecJson](raw: UntypedEventData): ValidS[(K, EventData[E])] = {
    val version = Version(raw.aggregateVersion)

    for {
      id <- raw.aggregateId.decodeEither[K]
      event <- raw.eventData.decodeEither[E]
    } yield (id, EventData(version, event, raw.createdAt))
  }
}
