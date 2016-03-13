package fairshare.backend.eventsourcing

sealed trait Snapshot[+A]

case object Void extends Snapshot[Nothing]
case class Ill(cause: String) extends Snapshot[Nothing]
case class Healthy[A](model: A, revision: Revision) extends Snapshot[A]

