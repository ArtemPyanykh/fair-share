package repo

import cqrs._
import util.Id
import util.types._
import scalaz.syntax.either._
import scalaz.std.java.throwable._

import scalaz.{ Show, \/ }

trait AggregateRepo[A, AA <: Aggregate[A]] {
  def getAggregate(id: Id[A]): Throwable \/ Option[AA]

  def storeAggregate(a: AA): Throwable \/ Unit

  def getAggregateV(id: Id[A]): ValidS[AA] = getAggregate(id).fold(
    Show[Throwable].shows(_).left,
    {
      case Some(a) => a.right
      case None => "Object not found".left
    }
  )

  def storeAggregateV(a: AA): ValidS[Unit] = storeAggregate(a).leftMap(Show[Throwable].shows)
}

