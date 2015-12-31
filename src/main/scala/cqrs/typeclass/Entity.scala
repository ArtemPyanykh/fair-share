package cqrs.typeclass

import simulacrum._
import util.Id

@typeclass trait Entity[A] {
  def id(a: A): Id[A]
}
