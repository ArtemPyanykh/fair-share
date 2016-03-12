package eventsourcing

import java.time.LocalDateTime

import argonaut.EncodeJson

import scalaz.Order

case class Fact[E](
  index: Index,
  subject: Subject,
  revision: Revision,
  event: E,
  createdAt: LocalDateTime
)

case class Index(num: Int) extends AnyVal {
  def next: Index = Index(num + 1)
}

object Index {
  def initial: Index = Index(0)

  implicit val order: Order[Index] = Order.fromScalaOrdering[Int].contramap(_.num)
}

case class Subject(key: String) extends AnyVal

case object Subject {
  implicit val encodeJson: EncodeJson[Subject] = EncodeJson.of[String].contramap(_.key)
}

case class Revision(revision: Int) extends AnyVal {
  def next: Revision = Revision(revision + 1)
}

object Revision {
  def initial: Revision = Revision(0)

  implicit val order: Order[Revision] = Order.fromScalaOrdering[Int].contramap(_.revision)
}
