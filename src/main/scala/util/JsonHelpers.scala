package util

import argonaut._
import argonaut.Argonaut._

trait JsonHelpers {
  def taggedEncode[A](tag: String)(encode: EncodeJson[A]): EncodeJson[A] = EncodeJson[A] {
    obj => Json(tag := encode.encode(obj))
  }

  def taggedDecode[A](tag: String)(decode: DecodeJson[A]): DecodeJson[A] = DecodeJson {
    cursorOuter =>
      (cursorOuter --\ tag).hcursor match {
        case None => DecodeResult.fail[A](s"Unexpected tag: $tag", cursorOuter.history)
        case Some(cursorInner) => decode.decode(cursorInner)
      }
  }
}
