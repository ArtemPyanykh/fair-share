package es.typeclass

import simulacrum._

@typeclass trait Tag[A] {
  def tag(a: A): String
}
