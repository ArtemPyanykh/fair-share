package cqrs

case class Events[C, M](creation: EventData[C], modifications: Vector[EventData[M]]) {
  def withEvent(data: EventData[M]): Events[C, M] = copy(modifications = modifications :+ data)
}
