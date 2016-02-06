package repo

import java.sql.Timestamp
import java.time.LocalDateTime

import doobie.imports._

import scalaz.\/
import scalaz.concurrent.Task
import scalaz.std.vector._
import scalaz.syntax.either._

class PostgresRepo extends UntypedEventRepo {
  implicit val localDateTimeMeta: Meta[LocalDateTime] =
    Meta[Timestamp].nxmap(
      ts => ts.toLocalDateTime,
      ldt => Timestamp.valueOf(ldt)
    )

  override def storeAll(data: Vector[UntypedEventData]): \/[Throwable, Int] = {
    val req =
      s"""
        |insert into events (
        |  aggregate_tag, aggregate_id, aggregate_version, event_data, created_at
        |) values (?, ?, ?, ?, ?)
      """.stripMargin

    val xa = DriverManagerTransactor[Task](
      "org.postgresql.Driver", "jdbc:postgresql:fair-share", "fair-share", "password"
    )

    Update[UntypedEventData](req).updateMany(data).transact(xa).run.right[Throwable]
  }

  override def getBy(tag: String, id: String): \/[Throwable, Vector[UntypedEventData]] = {
    val xa = DriverManagerTransactor[Task](
      "org.postgresql.Driver", "jdbc:postgresql:fair-share", "fair-share", "password"
    )

    val query =
      s"""
        |SELECT
        |  aggregate_tag, aggregate_id, aggregate_version, event_data, created_at
        |FROM events
        |WHERE aggregate_tag = '$tag' AND aggregate_id = '$id'
      """.stripMargin

    val query1: Query0[UntypedEventData] = Query[Unit, UntypedEventData](query).toQuery0(())

    query1.list.transact(xa).run.toVector.right[Throwable]
  }
}
