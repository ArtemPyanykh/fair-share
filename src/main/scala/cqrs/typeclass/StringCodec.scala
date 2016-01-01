package cqrs.typeclass

import simulacrum._
import util.types._

@typeclass trait StringCodec[E] {
  def encode(e: E): String

  def decode(string: String): ValidS[E]
}
