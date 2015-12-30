package cqrs

sealed trait Event[A]

trait CreationEvent[A] {
  def run: A
}

trait ChangeEvent[A] {
  def run(a: A): A
}
