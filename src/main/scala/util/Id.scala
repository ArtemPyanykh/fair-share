package util

import java.util.UUID

import argonaut.Argonaut._
import argonaut._

trait Id[T] {
  def id: UUID

  override def equals(obj: Any): Boolean = obj match {
    case x: Id[T] => this.id == x.id
    case _ => super.equals(obj)
  }

  override def hashCode(): Int = id.hashCode()
}

object Id {
  def apply[T](anId: UUID): Id[T] = new Id[T] {
    def id = anId
  }

  implicit def encodeJson[T]: EncodeJson[Id[T]] = EncodeJson.of[String].contramap(_.id.toString)

  implicit def decodeJson[T]: DecodeJson[Id[T]] = DecodeJson.of[String].map(s => Id[T](UUID.fromString(s)))

  implicit def jsonCodec[T]: CodecJson[Id[T]] = CodecJson.derived[Id[T]](encodeJson, decodeJson)
}

