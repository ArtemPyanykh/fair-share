package util

import scalaz.{ Kleisli, \/ }

trait Types {
  type Result[E, T] = E \/ T

  type ValidS[T] = Result[String, T]

  type CreateValidS[A] = Kleisli[ValidS, Unit, A]

  def CreateValidS[A] = Kleisli[ValidS, Unit, A] _ // scalastyle:off

  type ChangeValidS[A] = Kleisli[ValidS, A, A]

  def ChangeValidS[A] = Kleisli[ValidS, A, A] _ // scalastyle:off
}
