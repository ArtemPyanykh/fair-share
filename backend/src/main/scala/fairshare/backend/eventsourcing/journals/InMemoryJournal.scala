package fairshare.backend.eventsourcing.journals

import java.time.LocalDateTime

import fairshare.backend.eventsourcing._

import scala.collection.mutable
import scalaz.concurrent.Task
import scalaz.stream.Process
import scalaz.syntax.order._

class InMemoryJournal[E] extends Journal[E] {
  private var store = mutable.Buffer.empty[Fact[E]]
  private val lookup = mutable.Map[(Subject, Revision), Index]()
  private var entriesCount = 0

  def readAll(from: Index, to: Index): Process[Task, Fact[E]] =
    Process.emitAll(store.filter(e => e.index >= from && e.index <= to))

  def readSubject(key: Subject): Process[Task, Fact[E]] = {
    Process.emitAll(store.filter(e => e.subject == key))
  }

  def write(key: Subject, revision: Revision, data: E): Task[WriteResult[E]] = Task.delay {
    this.synchronized {
      lookup.get((key, revision)) match {
        case None =>
          val nextEntryNumber = Index(entriesCount)
          val newEl = Fact(nextEntryNumber, key, revision, data, LocalDateTime.now())

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

object InMemoryJournal {
  def apply[E]: InMemoryJournal[E] = new InMemoryJournal[E]
}
