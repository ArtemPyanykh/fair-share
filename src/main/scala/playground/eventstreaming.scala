package playground

import java.time.LocalDateTime
import scala.collection.mutable
import scalaz.Order
import scalaz.syntax.order._

case class SequenceNumber(num: Long) extends AnyVal

object SequenceNumber {
  def initial: SequenceNumber = SequenceNumber(0)

  implicit val order: Order[SequenceNumber] = Order.fromScalaOrdering[Long].contramap(_.num)
}

case class StreamKey(key: String) extends AnyVal

case class StreamRevision(revision: Long) extends AnyVal

object StreamRevision {
  def initial: StreamRevision = StreamRevision(0)

  implicit val order: Order[StreamRevision] = Order.fromScalaOrdering[Long].contramap(_.revision)
}

case class StreamElement[E](
  seqNr: SequenceNumber,
  key: StreamKey,
  revision: StreamRevision,
  data: E,
  createdAt: LocalDateTime
)

trait StreamReader[E] {
  def readAll(from: SequenceNumber): Vector[StreamElement[E]]
  def readKey(key: StreamKey, from: StreamRevision): Vector[StreamElement[E]]
}

trait StreamWriter[E] {
  def write(key: StreamKey, expectedRev: StreamRevision, data: E): WriteResult[E]
}

case class EventStore[E](reader: StreamReader[E], writer: StreamWriter[E])

sealed trait WriteResult[E]

case class WriteSuccess[E](committedEl: StreamElement[E]) extends WriteResult[E]
case class WriteFailure[E](conflictingEl: StreamElement[E]) extends WriteResult[E]

class InMemoryStreamReader[E](val storage: mutable.ArrayBuffer[StreamElement[E]]) extends StreamReader[E] {
  def readAll(from: SequenceNumber): Vector[StreamElement[E]] =
    storage.filter(_.seqNr >= from).toVector

  def readKey(key: StreamKey, from: StreamRevision): Vector[StreamElement[E]] =
    storage.filter(_.key == key).filter(_.revision >= from).toVector
}

class InMemoryStreamWriter[E](val storage: mutable.ArrayBuffer[StreamElement[E]]) extends StreamWriter[E] {
  def write(key: StreamKey, expectedRev: StreamRevision, data: E): WriteResult[E] = storage.synchronized {
    val existing = storage.find(el => el.key == key && el.revision == expectedRev)
    existing match {
      case None =>
        val nextSeqNr = SequenceNumber((storage.length + 1).toLong)
        val el = StreamElement(nextSeqNr, key, expectedRev, data, LocalDateTime.now())
        storage += el
        WriteSuccess(el)
      case Some(el) => WriteFailure(el)
    }
  }
}
