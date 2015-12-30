package cqrs

trait ChangeHandler[A, M] {
  def apply(a: A, changeEvent: M): A
}

object ChangeHandler {
  def apply[A, M](f: (A, M) => A): ChangeHandler[A, M] = new ChangeHandler[A, M] {
    def apply(a: A, changeEvent: M): A = f(a, changeEvent)
  }
}

trait CreationHandler[A, C] {
  def apply(createEvent: C): A
}

object CreationHandler {
  def apply[A, C](f: C => A): CreationHandler[A, C] = new CreationHandler[A, C] {
    def apply(createEvent: C): A = f(createEvent)
  }
}
