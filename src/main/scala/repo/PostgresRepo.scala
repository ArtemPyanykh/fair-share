package repo

import java.time.LocalDateTime

import doobie.imports._
import doobie.contrib.postgresql.imports._

import scalaz.\/
import scalaz.concurrent.Task
import scalaz.syntax.either._
import scalaz.std.vector._

class PostgresRepo extends UntypedEventRepo {
  implicit val localDateTimeMeta: Meta[LocalDateTime] =
    Meta[java.sql.Timestamp].nxmap(
      ts => ts.toLocalDateTime,
      ldt => java.sql.Timestamp.valueOf(ldt)
    )

  override def storeAll(data: Vector[UntypedEventData]): \/[Throwable, Unit] = {
    val sql =
      """
        |insert into events (
        |  aggregate_tag, aggregate_id, aggregate_version, event_data, created_at
        |) values (?, ?, ?, ?, ?)
      """.stripMargin

    val xa = DriverManagerTransactor[Task](
      "org.postgresql.Driver", "jdbc:postgresql:fair-share", "fair-share", "password"
    )

    import xa.yolo._

    Update[UntypedEventData](sql).updateMany(data).quick.run.right[Throwable]
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

    val qr: Query0[UntypedEventData] = Query[Unit, UntypedEventData](query).toQuery0(())

    qr.list.transact(xa).run.toVector.right[Throwable]
  }
}
