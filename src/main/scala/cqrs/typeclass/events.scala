package cqrs.typeclass

trait EventC[A, C] {
  def apply(event: C): A
}

object EventC {
  def apply[A, C](f: C => A): EventC[A, C] = new EventC[A, C] {
    def apply(event: C): A = f(event)
  }

  class EventCOps[A, C](event: C, ec: EventC[A, C]) {
    def run: A = ec(event)
  }

  object syntax { // scalastyle:off
    implicit def toEventCOps[A, C](event: C)(implicit ec: EventC[A, C]) = new EventCOps(event, ec)
  }
}

trait EventM[A, M] {
  def apply(model: A, event: M): A
}

object EventM {
  def apply[A, M](f: A => M => A): EventM[A, M] = new EventM[A, M] {
    def apply(model: A, event: M): A = f(model)(event)
  }

  class EventMOps[A, M](event: M, em: EventM[A, M]) {
    def run(model: A): A = em(model, event)
  }

  object syntax { // scalastyle:off
    implicit def toEventMOps[A, M](event: M)(implicit em: EventM[A, M]) = new EventMOps(event, em)
  }
}
