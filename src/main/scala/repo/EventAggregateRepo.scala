package repo

import cqrs.Entity.syntax._
import cqrs._
import util.Id

import scalaz.\/

class EventAggregateRepo[A: Entity, C, M](eventRepo: EventRepo[Id[A], C, M])
  (implicit ch: CreationHandler[A, C], chh: ChangeHandler[A, M]) extends AggregateRepo[A, EventAggregate[A, C, M]] {
  def getAggregate(id: Id[A]): Throwable \/ Option[EventAggregate[A, C, M]] = for {
    events <- eventRepo.getAll(id)
    (creationOpt, changes) = events
  } yield {
    for {
      creation <- creationOpt
    } yield EventSourcedBuilder.foldAggregate(creation.event, changes.map(_.event))
  }

  def storeAggregate(a: EventAggregate[A, C, M]): (Throwable \/ Unit) = {
    val (startingVersion, toPersistCreation) = if (a.version.isZero) {
      (Version.one, Some(EventData.now(a.model.id, Version.one, a.creationEvent)))
    } else (a.version, None)

    val numPersistedChanges = List(startingVersion.v - 2, 0).max

    val toPersistChanges = a.changeEvents.drop(numPersistedChanges).zipWithIndex.map { case (ev, idx) =>
      EventData.now(a.model.id, Version(numPersistedChanges + idx), ev)
    }

    toPersistCreation match {
      case Some(creation) => eventRepo.storeAll(a.model.id, creation, toPersistChanges)
      case None => eventRepo.storeAll(a.model.id, toPersistChanges)
    }
  }
}
