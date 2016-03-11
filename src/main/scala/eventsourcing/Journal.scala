package eventsourcing

import scalaz.concurrent.Task
import scalaz.stream._

trait Journal[E] {
  def readAll(from: EntryNumber, to: EntryNumber): Process[Task, Entry[E]]
  def readSubject(key: Subject): Process[Task, Entry[E]]
  def write(key: Subject, revision: Revision, data: E): Task[WriteResult[E]]
}

sealed trait WriteResult[+E]
case class WriteSuccess[E](committed: Entry[E]) extends WriteResult[E]
case class WriteFailure(cause: String) extends WriteResult[Nothing]
