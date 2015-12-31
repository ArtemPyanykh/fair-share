package cqrs

import cqrs.typeclass.{ EventM, EventC }
import EventC.syntax._
import EventM.syntax._

class AggregateBuilder[A, C: EventC[A, ?], M: EventM[A, ?]] {
  def foldModel(createEvent: C, changeEvents: Vector[M]): A = {
    applyToModel(createEvent.run, changeEvents)
  }

  def applyToModel(a: A, changeEvents: Vector[M]): A = {
    changeEvents.foldLeft(a) { case (acc, event) => event.run(acc) }
  }

  def foldAggregate(events: Events[C, M]): Aggregate[A, C, M] = {
    val data = foldModel(events.creation, events.modifications)
    val version = Version(events.modifications.length + 1) // # of change events + creation event
    Aggregate(version, data, events)
  }
}

object AggregateBuilder {
  def apply[A, C: EventC[A, ?], M: EventM[A, ?]]: AggregateBuilder[A, C, M] =
    new AggregateBuilder[A, C, M]
}
