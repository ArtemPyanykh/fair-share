package cqrs

import cqrs.typeclass.{ EventM, EventC }
import EventC.syntax._
import EventM.syntax._

class AggregateCommand[A, C: EventC[A, ?], M: EventM[A, ?]] {
  def create(event: C): Aggregate[A, C, M] = {
    val model = event.run
    Aggregate(Version.zero, model, Events(event, Vector.empty))
  }

  def modify(agg: Aggregate[A, C, M], event: M): Aggregate[A, C, M] = {
    val changedModel = event.run(agg.model)
    val changedEvents = agg.events.withEvent(event)

    agg.copy(version = agg.version.next, model = changedModel, events = changedEvents)
  }
}

object AggregateCommand {
  def apply[A, C: EventC[A, ?], M: EventM[A, ?]]: AggregateCommand[A, C, M] =
    new AggregateCommand
}
