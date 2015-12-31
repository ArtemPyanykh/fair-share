package repo

import cqrs._
import cqrs.typeclass._
import util.Id
import util.types._
import scalaz.std.java.throwable._
import cqrs.typeclass.Tagged.ops._
import scalaz.syntax.either._
import cqrs.typeclass.Entity.ops._

import scalaz.Show

class GeneralAggregateRepo[A: Entity, C: EventC[A, ?]: StringCodec, M: EventM[A, ?]: StringCodec](
    eventRepo: GeneralEventRepo[Id[A], C, M]
)(implicit idTagged: Tagged[Id[A]]) {
  def getAggregate(id: Id[A]): ValidS[Aggregate[A, C, M]] = {
    val fromRepo = eventRepo.getAll(id)

    fromRepo.fold(
      Show[Throwable].shows(_).left,
      {
        case None => s"Aggregate ${id.tag} with id ${id.id.toString} not found.".left
        case Some(data) =>
          val events = data
          AggregateBuilder[A, C, M].foldAggregate(events).right
      }
    )
  }

  def storeAggregate(a: Aggregate[A, C, M]): ValidS[Unit] = {
    eventRepo.storeAll(a.model.id, a.events, a.persistedVersion).leftMap(Show[Throwable].shows)
  }

}
