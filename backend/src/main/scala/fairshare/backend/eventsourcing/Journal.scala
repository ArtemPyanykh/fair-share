package fairshare.backend.eventsourcing

import fairshare.backend.util.Iso

import scalaz.concurrent.Task
import scalaz.stream._

trait Journal[E] extends JournalFunctions[E] {
  def readAll(from: Index, to: Index): Process[Task, Fact[E]]

  def readSubject(key: Subject): Process[Task, Fact[E]]

  def write(key: Subject, revision: Revision, data: E): Task[WriteResult[E]]
}

sealed trait WriteResult[+E]

case class WriteSuccess[E](committed: Fact[E]) extends WriteResult[E]

case class WriteFailure(cause: String) extends WriteResult[Nothing]

trait JournalFunctions[E] {
  self: Journal[E] =>

  def readSnapshot[A, I](id: I)(implicit iso: Iso[I, Subject], processor: FactProcessor[A, E]): Task[Snapshot[A]] = {
    readSubject(iso.to(id)).fold[Snapshot[A]](Void) {
      (snap, entry) => processor.processEntry(entry, snap)
    }.runLastOr(Void)
  }

  def writeI[I](key: I, revision: Revision, data: E)(implicit iso: Iso[I, Subject]): Task[WriteResult[E]] =
    write(iso.to(key), revision, data)
}
