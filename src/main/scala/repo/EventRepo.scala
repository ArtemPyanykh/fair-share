package repo

import cqrs.typeclass.{ Tagged, StringCodec }
import cqrs.{ Version, EventData, Events }

import scalaz.\/
import cqrs.typeclass.Tagged.ops._
import cqrs.typeclass.StringCodec.ops._

class EventRepo[K: StringCodec: Tagged, C: StringCodec: Tagged, M: StringCodec: Tagged](untypedRepo: UntypedEventRepo) {
  type DataEvents = Events[EventData[C], EventData[M]]

  def getAll(id: K): Throwable \/ Option[DataEvents] = {
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
        Events[EventData[C], EventData[M]](typedCreation, typedModifications)
      }
    }
  }

  def storeAll(id: K, events: DataEvents, alreadyPersisted: Version): Throwable \/ Unit = {
    val untypedCreation = UntypedEventData.fromEventData(id, events.creation)
    val untypedModifications = events.modifications.map(e => UntypedEventData.fromEventData(id, e))
    val untypedEvents = untypedCreation +: untypedModifications

    val toStore = untypedEvents.drop(alreadyPersisted.value)

    untypedRepo.store(toStore)
  }
}
