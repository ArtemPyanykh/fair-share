package es.typeclass

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
