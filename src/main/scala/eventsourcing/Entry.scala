package eventsourcing

import java.time.LocalDateTime

import scalaz.Order

case class Entry[E](
  number: EntryNumber,
  subject: Subject,
  revision: Revision,
  event: E,
  createdAt: LocalDateTime
)

case class EntryNumber(num: Int) extends AnyVal {
  def next: EntryNumber = EntryNumber(num + 1)
}

object EntryNumber {
  def initial: EntryNumber = EntryNumber(0)

  implicit val order: Order[EntryNumber] = Order.fromScalaOrdering[Int].contramap(_.num)
}

case class Subject(key: String) extends AnyVal

case class Revision(revision: Int) extends AnyVal {
  def next: Revision = Revision(revision + 1)
}

object Revision {
  def initial: Revision = Revision(0)

  implicit val order: Order[Revision] = Order.fromScalaOrdering[Int].contramap(_.revision)
}
