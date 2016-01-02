package es.typeclass

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
