package fairshare.backend.eventsourcing

abstract class FactProcessor[A, E] {
  def processEntry: (Fact[E], Snapshot[A]) => Snapshot[A]

  def processEntries(aggregate: Snapshot[A], entries: List[Fact[E]]): Snapshot[A] =
    entries.foldLeft(aggregate)((acc, e) => processEntry(e, acc))
}

object FactProcessor {
  def apply[A, E](implicit processor: FactProcessor[A, E]): FactProcessor[A, E] = processor

  def attemptCreate[A](f: => A): Snapshot[A] => Snapshot[A] = {
    case x @ Ill(_) => x
    case Void => Healthy(f, Revision.initial)
    case x @ Healthy(_, _) => Ill(s"Tried to create already created.")
  }

  def attemptModify[A](f: A => A): Snapshot[A] => Snapshot[A] = {
    case x @ Ill(_) => x
    case x @ Void => Ill(s"Tried to modify not yet created.")
    case Healthy(model, revision) => Healthy(f(model), revision.next)
  }

  def basedOnEvent[A, E](eventF: E => Snapshot[A] => Snapshot[A]): (Fact[E], Snapshot[A]) => Snapshot[A] =
    (entry, snap) => snap match {
      case x @ Void =>
        if (entry.revision == Revision.initial) {
          eventF(entry.event)(snap)
        } else {
          Ill(s"Journal is broken: first entry starts with ${entry.revision} revision.")
        }

      case x @ Healthy(_, _) =>
        if (entry.revision == x.revision.next) {
          eventF(entry.event)(snap)
        } else {
          Ill(s"Journal is broken: tried to apply revision ${entry.revision} to a snapshot of revision ${x.revision}")
        }

      case x @ Ill(_) => x
    }

  def BasedOnEvents[A, E](eventF: E => Snapshot[A] => Snapshot[A]): FactProcessor[A, E] = new FactProcessor[A, E] { // scalastyle:ignore
    def processEntry: (Fact[E], Snapshot[A]) => Snapshot[A] = basedOnEvent(eventF)
  }
}

