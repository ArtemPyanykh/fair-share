package repo

import argonaut.CodecJson
import es._
import es.typeclass._
import util.Id
import util.types._
import scalaz.std.java.throwable._
import es.typeclass.Tag.ops._
import scalaz.syntax.either._
import es.typeclass.Entity.ops._

import scalaz.Show

class GeneralAggregateRepo[A: Entity, C: EventC[A, ?]: CodecJson, M: EventM[A, ?]: CodecJson](
    eventRepo: GeneralEventRepo[Id[A], C, M]
)(implicit idTagged: Tag[Id[A]]) {
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
