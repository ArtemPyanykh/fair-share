package util

import argonaut.Argonaut._
import argonaut.{ DecodeJson, EncodeJson }
import cqrs.typeclass.StringCodec
import util.types._

trait StringCodecHelpers {
  def fromJsonCodec[A](encodeJson: EncodeJson[A], decodeJson: DecodeJson[A]): StringCodec[A] = new StringCodec[A] {
    def encode(x: A): String = encodeJson.encode(x).nospaces

    def decode(string: String): ValidS[A] = string.decodeEither[A](decodeJson)
  }
}
