package es

case class Aggregate[A, C, M](
  persistedVersion: Version,
  currentVersion: Version,
  model: A,
  events: Events[C, M]
)

