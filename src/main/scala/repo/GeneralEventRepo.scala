package repo

import argonaut.CodecJson
import es.typeclass.Tag.ops._
import es.typeclass.Tag
import es.{ Events, Version }
import argonaut.Argonaut._

import scalaz.\/

class GeneralEventRepo[K: CodecJson: Tag, C: CodecJson, M: CodecJson](
    untypedRepo: UntypedEventRepo
) {
  def getAll(id: K): Throwable \/ Option[Events[C, M]] = {
    val aggregateTag = id.tag
    val aggregateId = id.asJson.nospaces

    for {
      untypedData <- untypedRepo.getBy(aggregateTag, aggregateId)
    } yield {
      for {
        creation <- untypedData.headOption
        modification = untypedData.tail
      } yield {
        val typedCreation = UntypedEventData.toEventData[K, C](creation).fold(
          sys.error, _._2
        )
        val typedModifications = modification.map(e => UntypedEventData.toEventData[K, M](e).fold(
          sys.error, _._2
        ))

        Events(typedCreation, typedModifications)
      }
    }
  }

  def storeAll(id: K, events: Events[C, M], alreadyPersisted: Version): Throwable \/ Unit = {
    val untypedCreation = UntypedEventData.fromEventData(id, events.creation)
    val untypedModifications = events.modifications.map(e => UntypedEventData.fromEventData(id, e))
    val untypedEvents = untypedCreation +: untypedModifications

    val toStore = untypedEvents.drop(alreadyPersisted.value)

    untypedRepo.storeAll(toStore)
  }
}
