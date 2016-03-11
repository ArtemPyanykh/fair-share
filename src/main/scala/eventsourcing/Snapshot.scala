package eventsourcing

sealed trait Snapshot[+A]

case object Void extends Snapshot[Nothing]
case class Ill[A](previousNonIll: Snapshot[A], cause: String) extends Snapshot[Nothing]
case class Healthy[A](model: A, revision: Revision) extends Snapshot[A]

