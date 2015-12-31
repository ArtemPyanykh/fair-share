package cqrs

import cqrs.typeclass.{ EventM, EventC }
import EventC.syntax._
import EventM.syntax._

class AggregateCommand[A, C: EventC[A, ?], M: EventM[A, ?]] {
  def create(event: C): Aggregate[A, C, M] = {
    val model = event.run
    val data = EventData.now(Version.one, event)
    Aggregate(Version.zero, Version.one, model, Events(data, Vector.empty))
  }

  def modify(agg: Aggregate[A, C, M], event: M): Aggregate[A, C, M] = {
    val changedModel = event.run(agg.model)
    val data = EventData.now(agg.currentVersion.next, event)
    val changedEvents = agg.events.withEvent(data)

    agg.copy(currentVersion = agg.currentVersion.next, model = changedModel, events = changedEvents)
  }
}

object AggregateCommand {
  def apply[A, C: EventC[A, ?], M: EventM[A, ?]]: AggregateCommand[A, C, M] =
    new AggregateCommand
}
