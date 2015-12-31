package cqrs

case class Aggregate[A, C, M](
  version: Version,
  model: A,
  events: Events[C, M]
)

