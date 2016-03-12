package util

import java.util.UUID

import eventsourcing.Subject
import shapeless.Lazy

trait Iso[A, B] {
  self =>

  def to: A => B

  def from: B => A

  def mapLeft[C](f: B => C)(g: C => B): Iso[A, C] = new Iso[A, C] {
    def to: A => C = self.to.andThen(f)

    def from: C => A = g.andThen(self.from)
  }

  def mapRight[C](f: C => A)(g: A => C): Iso[C, B] = new Iso[C, B] {
    def to: C => B = f.andThen(self.to)

    def from: B => C = self.from.andThen(g)
  }
}

object Iso extends IsoImplicits1 with IsoImplicits2 {
  def apply[A, B](implicit iso: Iso[A, B]): Iso[A, B] = iso

  def build[A, B](toF: A => B, fromF: B => A): Iso[A, B] = new Iso[A, B] {
    def to: A => B = toF

    def from: B => A = fromF
  }
}

trait IsoImplicits1 {
  implicit val uuidToSubjectIso: Iso[UUID, Subject] = new Iso[UUID, Subject] {
    def to: UUID => Subject = id => Subject(id.toString)

    def from: Subject => UUID = s => UUID.fromString(s.key)
  }

  def transitiveUuidToSubjectIso[A](implicit iso: Lazy[Iso[A, UUID]]): Iso[A, Subject] = new Iso[A, Subject] {
    def to: A => Subject = iso.value.to.andThen(uuidToSubjectIso.to)

    def from: Subject => A = uuidToSubjectIso.from.andThen(iso.value.from)
  }
}

trait IsoImplicits2 {
  implicit def commutative[A, B](implicit iso: Lazy[Iso[A, B]]): Iso[B, A] = new Iso[B, A] {
    def to: B => A = iso.value.from

    def from: A => B = iso.value.to
  }
}
