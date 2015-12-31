package repo

import cqrs._
import cqrs.typeclass._
import util.Id
import util.types._
import scalaz.std.java.throwable._
import cqrs.typeclass.Tagged.ops._
import scalaz.syntax.either._

import scalaz.Show

class EventAggregateRepo[A: Entity, C: EventC[A, ?]: StringCodec, M: EventM[A, ?]: StringCodec](
    eventRepo: EventRepo[Id[A], C, M]
)(implicit idTagged: Tagged[Id[A]]) {
  def getAggregate(id: Id[A]): ValidS[Aggregate[A, C, M]] = {
    val fromRepo = eventRepo.getAll(id)

    fromRepo.fold(
      Show[Throwable].shows(_).left,
      {
        case None => s"Aggregate ${id.tag} with id ${id.id.toString} not found.".left
        case Some(data) =>
          val events = data.mapCM(_.event, _.event)
          AggregateBuilder[A, C, M].foldAggregate(events).right
      }
    )
  }

  def storeAggregate(a: Aggregate[A, C, M]): ValidS[Unit] = ???

}
