package cqrs

object EventSourcedBuilder {
  def foldModel[A, C, M](createEvent: C, changeEvents: Vector[M])
    (implicit create: CreationHandler[A, C], change: ChangeHandler[A, M]): A = {
    foldModel[A, M](create(createEvent), changeEvents)
  }

  def foldModel[A, M](a: A, changeEvents: Vector[M])(implicit change: ChangeHandler[A, M]): A = {
    changeEvents.foldLeft(a) { case (acc, event) => change(acc, event) }
  }

  def foldAggregate[A, C, M](createEvent: C, changeEvents: Vector[M])
    (implicit create: CreationHandler[A, C], change: ChangeHandler[A, M]): EventAggregate[A, C, M] = {
    val data = foldModel[A, M](create(createEvent), changeEvents)
    val version = Version(changeEvents.length + 1) // # of change events + creation event
    EventAggregate(version, data, createEvent, changeEvents)
  }
}
