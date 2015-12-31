package cqrs.typeclass

import simulacrum._

@typeclass trait Tagged[A] {
  def tag(a: A): String
}
