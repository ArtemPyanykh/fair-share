package playground

import java.time.LocalDateTime

import scala.collection.mutable
import scalaz.Order
import scalaz.concurrent.Task
import scalaz.stream.Process
import scalaz.syntax.order._

case class Fact[E](
  number: FactNumber,
  subject: FactSubject,
  revision: SubjectRevision,
  data: E,
  createdAt: LocalDateTime
)

case class FactNumber(num: Int) extends AnyVal {
  def next: FactNumber = FactNumber(num + 1)
}

object FactNumber {
  def initial: FactNumber = FactNumber(0)

  implicit val order: Order[FactNumber] = Order.fromScalaOrdering[Int].contramap(_.num)
}

case class FactSubject(key: String) extends AnyVal

case class SubjectRevision(revision: Int) extends AnyVal

object SubjectRevision {
  def initial: SubjectRevision = SubjectRevision(0)

  implicit val order: Order[SubjectRevision] = Order.fromScalaOrdering[Int].contramap(_.revision)
}

trait Journal[E] {
  def readAll(from: FactNumber, to: FactNumber): Process[Task, Fact[E]]
  def readSubject(key: FactSubject): Process[Task, Fact[E]]
  def write(key: FactSubject, revision: SubjectRevision, data: E): Task[WriteResult[E]]
}

sealed trait WriteResult[+E]
case class WriteSuccess[E](committedEl: Fact[E]) extends WriteResult[E]
case class WriteFailure(cause: String) extends WriteResult[Nothing]

class InMemoryJournal[E](underlying: mutable.Buffer[Fact[E]]) extends Journal[E] {
  private var curNumber = FactNumber(underlying.length)
  private val lookup = mutable.Map[(FactSubject, SubjectRevision), FactNumber]()

  def readAll(from: FactNumber, to: FactNumber): Process[Task, Fact[E]] =
    Process.emitAll(underlying.filter(e => e.number >= from && e.number <= to))

  def readSubject(key: FactSubject): Process[Task, Fact[E]] = {
    Process.emitAll(underlying.filter(e => e.subject == key))
  }

  def write(key: FactSubject, revision: SubjectRevision, data: E): Task[WriteResult[E]] = Task.delay {
    this.synchronized {
      lookup.get((key, revision)) match {
        case None =>
          lookup += (key, revision) -> curNumber
          val newEl = Fact(curNumber, key, revision, data, LocalDateTime.now())
          underlying += newEl
          curNumber = curNumber.next
          WriteSuccess(newEl)
        case Some(seqNr) =>
          WriteFailure(s"Stream element ($key, $revision) already exists")
      }
    }
  }
}
