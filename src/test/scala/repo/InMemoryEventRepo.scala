package repo

import scala.collection.mutable
import scalaz.\/
import scalaz.syntax.either._

class InMemoryEventRepo extends UntypedEventRepo {
  type Tag = String
  type Id = String
  type EventStream = Vector[UntypedEventData]

  val inMemoryStore = mutable.Map[Tag, mutable.Map[Id, EventStream]]()

  override def storeAll(data: Vector[UntypedEventData]): Throwable \/ Int = inMemoryStore.synchronized {
    val headOpt = data.headOption

    headOpt match {
      case None => 0.right[Throwable]
      case Some(datum) => {
        storeSingle(datum).fold(
          err => err.left,
          _ => storeAll(data.tail)
        )
      }
    }
  }

  override def getBy(tag: String, id: String): Throwable \/ Vector[UntypedEventData] = {
    inMemoryStore.getOrElse(tag, mutable.Map[Id, EventStream]()).getOrElse(id, Vector.empty).right
  }

  private def storeSingle(data: UntypedEventData): Throwable \/ Int = {
    val tag = data.aggregateTag
    val id = data.aggregateId
    val forAggregate = inMemoryStore.getOrElse(tag, mutable.Map[Id, EventStream]())
    val existing = forAggregate.getOrElse(id, Vector.empty)

    if (versionExists(existing, data.aggregateVersion)) {
      new RuntimeException(
        s"Uniqueness violation: version ${data.aggregateVersion} of aggregate ${data.aggregateTag} already exists."
      ).left
    } else {
      forAggregate += (id -> (existing :+ data))
      inMemoryStore += (tag -> forAggregate)
      1.right
    }
  }

  private def versionExists(data: Vector[UntypedEventData], version: Int): Boolean =
    data.exists(_.aggregateVersion == version)
}
