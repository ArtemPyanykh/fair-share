package cqrs.typeclass

import simulacrum._

@typeclass trait StringCodec[E] {
  def encode(e: E): String

  def decode(string: String): E
}
