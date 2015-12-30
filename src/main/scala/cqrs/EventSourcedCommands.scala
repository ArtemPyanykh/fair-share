package cqrs

object EventSourcedCommands {
  def create[A, C <: CreationEvent[A], M <: ChangeEvent[A]](event: C): EventAggregate[A, C, M] = {
    val model = event.run
    EventAggregate(Version.zero, model, event, Vector.empty)
  }

  def change[A, C <: CreationEvent[A], M <: ChangeEvent[A]](agg: EventAggregate[A, C, M], event: M): EventAggregate[A, C, M] = {
    val changedModel = event.run(agg.model)
    agg.copy(version = agg.version.next, model = changedModel, changeEvents = agg.changeEvents :+ event)
  }
}
