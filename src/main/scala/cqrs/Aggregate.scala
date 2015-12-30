package cqrs

sealed trait Aggregate[A] {
  def version: Version
  def model: A
}

case class RegularAggregate[A](
  version: Version,
  model: A
) extends Aggregate[A]

case class EventAggregate[A, C, M](
  version: Version,
  model: A,
  creationEvent: C,
  changeEvents: Vector[M]
) extends Aggregate[A]

