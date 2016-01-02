package util

import argonaut._
import argonaut.Argonaut._

trait JsonHelpers {
  def taggedEncode[A](tag: String)(f: A => Json): EncodeJson[A] = EncodeJson[A] {
    obj => Json(tag := f(obj))
  }

  def taggedJson(tag: String)(json: Json): Json = Json(tag := json)

  def taggedDecode[A](tag: String)(f: HCursor => DecodeResult[A]): DecodeJson[A] = DecodeJson {
    cursorOuter =>
      (cursorOuter --\ tag).hcursor match {
        case None => DecodeResult.fail[A](s"Specified tag $tag not found", cursorOuter.history)
        case Some(cursorInner) => f(cursorInner)
      }
  }
}
