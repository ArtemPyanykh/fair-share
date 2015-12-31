package repo

import cqrs.typeclass.StringCodec.ops._
import cqrs.typeclass.Tagged.ops._
import cqrs.typeclass.{ StringCodec, Tagged }
import cqrs.{ Events, Version }

import scalaz.\/

class GeneralEventRepo[K: StringCodec: Tagged, C: StringCodec: Tagged, M: StringCodec: Tagged](
    untypedRepo: UntypedEventRepo
) {
  def getAll(id: K): Throwable \/ Option[Events[C, M]] = {
    val aggregateTag = id.tag
    val aggregateId = id.encode

    for {
      untypedData <- untypedRepo.get(aggregateTag, aggregateId)
    } yield {
      for {
        creation <- untypedData.headOption
        modification = untypedData.tail
      } yield {
        val typedCreation = UntypedEventData.toEventData[K, C](creation)._2
        val typedModifications = modification.map(e => UntypedEventData.toEventData[K, M](e)._2)
        Events(typedCreation, typedModifications)
      }
    }
  }

  def storeAll(id: K, events: Events[C, M], alreadyPersisted: Version): Throwable \/ Unit = {
    val untypedCreation = UntypedEventData.fromEventData(id, events.creation)
    val untypedModifications = events.modifications.map(e => UntypedEventData.fromEventData(id, e))
    val untypedEvents = untypedCreation +: untypedModifications

    val toStore = untypedEvents.drop(alreadyPersisted.value)

    untypedRepo.store(toStore)
  }
}
