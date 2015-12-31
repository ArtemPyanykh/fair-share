package util

import java.util.UUID
import cqrs.typeclass.{ Tagged, StringCodec }

import scala.reflect.ClassTag

trait Id[T] {
  def id: UUID
}

object Id {
  def apply[T](anId: UUID): Id[T] = new Id[T] {
    def id = anId
  }

  implicit def stringCodec[T]: StringCodec[Id[T]] = new StringCodec[Id[T]] {
    def encode(e: Id[T]): String = e.id.toString

    def decode(string: String): Id[T] = Id[T](UUID.fromString(string))
  }

  implicit def tagged[T](implicit ct: ClassTag[T]): Tagged[Id[T]] = new Tagged[Id[T]] {
    def tag(a: Id[T]): String = ct.getClass.getName
  }
}

