package playground

sealed trait Snapshot[+A]

case object Void extends Snapshot[Nothing]
case class Ill[A](previous: Snapshot[A], cause: String) extends Snapshot[Nothing]
case class Healthy[A](model: A) extends Snapshot[A]

abstract class EventProcessor[A, E] {
  def processOne(event: E): Snapshot[A] => Snapshot[A]

  def process(aggregate: Snapshot[A], events: List[E]): Snapshot[A] =
    events.foldLeft(aggregate)((acc, e) => processOne(e)(acc))

  protected def attemptCreate(f: => A): Snapshot[A] => Snapshot[A] = EventProcessor.attemptCreate(f)
  protected def attemptModify(f: A => A): Snapshot[A] => Snapshot[A] = EventProcessor.attemptModify(f)
}

object EventProcessor {
  def attemptCreate[A](f: => A): Snapshot[A] => Snapshot[A] = {
    case x @ Ill(_, _) => x
    case Void => Healthy(f)
    case x @ Healthy(_) => Ill(x, s"Tried to create already created.")
  }

  def attemptModify[A](f: A => A): Snapshot[A] => Snapshot[A] = {
    case x @ Ill(_, _) => x
    case x @ Void => Ill(x, s"Tried to modify not yet created.")
    case Healthy(model) => Healthy(f(model))
  }
}

