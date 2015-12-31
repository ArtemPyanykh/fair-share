package repo

import util.Id
import util.types._

import scalaz.std.java.throwable._
import scalaz.syntax.either._
import scalaz.{ Show, \/ }

trait AggregateRepo[A, Aggregate[_]] {

  def getAggregate(id: Id[A]): Throwable \/ Option[Aggregate[A]]

  def storeAggregate(a: Aggregate[A]): Throwable \/ Unit

  def getAggregateV(id: Id[A]): ValidS[Aggregate[A]] = getAggregate(id).fold(
    Show[Throwable].shows(_).left,
    {
      case Some(a) => a.right
      case None => "Object not found".left
    }
  )

  def storeAggregateV(a: Aggregate[A]): ValidS[Unit] = storeAggregate(a).leftMap(Show[Throwable].shows)
}

