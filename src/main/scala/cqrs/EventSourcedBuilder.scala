package cqrs

object EventSourcedBuilder {
  def foldModel[A, C <: CreationEvent[A], M <: ChangeEvent[A]](createEvent: C, changeEvents: Vector[M]): A = {
    foldModel[A, M](createEvent.run, changeEvents)
  }

  def foldModel[A, M <: ChangeEvent[A]](a: A, changeEvents: Vector[M]): A = {
    changeEvents.foldLeft(a) { case (acc, event) => event.run(acc) }
  }

  def foldAggregate[A, C <: CreationEvent[A], M <: ChangeEvent[A]](createEvent: C, changeEvents: Vector[M]): EventAggregate[A, C, M] = {
    val data = foldModel[A, M](createEvent.run, changeEvents)
    val version = Version(changeEvents.length + 1) // # of change events + creation event
    EventAggregate(version, data, createEvent, changeEvents)
  }
}
