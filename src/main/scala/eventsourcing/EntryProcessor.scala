package eventsourcing

abstract class EntryProcessor[A, E] {
  def processEntry: (Entry[E], Snapshot[A]) => Snapshot[A]

  def processEntries(aggregate: Snapshot[A], entries: List[Entry[E]]): Snapshot[A] =
    entries.foldLeft(aggregate)((acc, e) => processEntry(e, acc))

  protected def attemptCreate(f: => A): Snapshot[A] => Snapshot[A] = EntryProcessor.attemptCreate(f)

  protected def attemptModify(f: A => A): Snapshot[A] => Snapshot[A] = EntryProcessor.attemptModify(f)

  protected def basedOnEvent(eventF: E => Snapshot[A] => Snapshot[A]) = EntryProcessor.basedOnEvent(eventF)
}

object EntryProcessor {
  def attemptCreate[A](f: => A): Snapshot[A] => Snapshot[A] = {
    case x @ Ill(_, _) => x
    case Void => Healthy(f, Revision.initial)
    case x @ Healthy(_) => Ill(x, s"Tried to create already created.")
  }

  def attemptModify[A](f: A => A): Snapshot[A] => Snapshot[A] = {
    case x @ Ill(_, _) => x
    case x @ Void => Ill(x, s"Tried to modify not yet created.")
    case x @ Healthy(model) => Healthy(f(x.model), x.revision.next)
  }

  def basedOnEvent[A, E](eventF: E => Snapshot[A] => Snapshot[A]): (Entry[E], Snapshot[A]) => Snapshot[A] =
    (entry, snap) => snap match {
      case x @ Void =>
        if (entry.revision == Revision.initial) {
          eventF(entry.event)(snap)
        } else {
          Ill(Void, s"Journal is broken: first entry starts with ${entry.revision} revision.")
        }

      case x @ Healthy(_, _) =>
        if (entry.revision == x.revision.next) {
          eventF(entry.event)(snap)
        } else {
          Ill(x, s"Journal is broken: tried to apply revision ${entry.revision} to a snapshot of revision ${x.revision}")
        }

      case x @ Ill(_, _) => x
    }
}

