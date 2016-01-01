package util

import java.util.UUID

import argonaut.Argonaut._
import argonaut._
import cqrs.typeclass.{ StringCodec, Tagged }
import util.types._

import scala.reflect.ClassTag

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

  implicit def jsonEncode[T]: EncodeJson[Id[T]] = jencode1[Id[T], String](_.id.toString)
  implicit def jsonDecode[T]: DecodeJson[Id[T]] = jdecode1[String, Id[T]](s => Id[T](UUID.fromString(s)))

  implicit def stringCodec[T]: StringCodec[Id[T]] = new StringCodec[Id[T]] {
    def encode(e: Id[T]): String = e.asJson.nospaces

    def decode(string: String): ValidS[Id[T]] = string.decodeEither[Id[T]]
  }

  implicit def tagged[T](implicit ct: ClassTag[T]): Tagged[Id[T]] = new Tagged[Id[T]] {
    def tag(a: Id[T]): String = ct.getClass.getName
  }
}

