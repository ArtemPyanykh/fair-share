package eventsourcing.journals

import java.time.LocalDateTime

import eventsourcing._

import scala.collection.mutable
import scalaz.concurrent.Task
import scalaz.stream.Process
import scalaz.syntax.order._

class InMemoryJournal[E] extends Journal[E] {
  private var store = mutable.Buffer.empty[Entry[E]]
  private val lookup = mutable.Map[(Subject, Revision), EntryNumber]()
  private var entriesCount = 0

  def readAll(from: EntryNumber, to: EntryNumber): Process[Task, Entry[E]] =
    Process.emitAll(store.filter(e => e.number >= from && e.number <= to))

  def readSubject(key: Subject): Process[Task, Entry[E]] = {
    Process.emitAll(store.filter(e => e.subject == key))
  }

  def write(key: Subject, revision: Revision, data: E): Task[WriteResult[E]] = Task.delay {
    this.synchronized {
      lookup.get((key, revision)) match {
        case None =>
          val nextEntryNumber = EntryNumber(entriesCount)
          val newEl = Entry(nextEntryNumber, key, revision, data, LocalDateTime.now())

          lookup += (key, revision) -> nextEntryNumber
          store += newEl
          entriesCount += 1

          WriteSuccess(newEl)
        case Some(_) =>
          WriteFailure(s"Stream element ($key, $revision) already exists")
      }
    }
  }
}
