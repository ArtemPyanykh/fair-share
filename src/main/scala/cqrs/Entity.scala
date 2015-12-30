package cqrs

import util.Id

trait Entity[A] {
  def id(a: A): Id[A]
}

object Entity {
  def apply[A](f: A => Id[A]): Entity[A] = new Entity[A] {
    def id(a: A): Id[A] = f(a)
  }

  class EntityOps[A](a: A)(implicit entity: Entity[A]) {
    def id: Id[A] = entity.id(a)
  }

  object syntax { // scalastyle:ignore
    implicit def toEntityOps[A](a: A)(implicit e: Entity[A]): EntityOps[A] = new EntityOps[A](a)
  }

}
