package cqrs

object EventSourcedCommands {
  def create[A, C, M](event: C)(implicit creationHandler: CreationHandler[A, C]): EventAggregate[A, C, M] = {
    val data = creationHandler(event)
    EventAggregate(Version.zero, data, event, Vector.empty)
  }

  def change[A, C, M](a: EventAggregate[A, C, M], event: M)
    (implicit changeHandler: ChangeHandler[A, M]): EventAggregate[A, C, M] = {
    val changedData = changeHandler(a.model, event)
    a.copy(version = a.version.next, model = changedData, changeEvents = a.changeEvents :+ event)
  }
}
