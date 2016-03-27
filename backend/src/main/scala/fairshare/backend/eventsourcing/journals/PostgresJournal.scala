package fairshare.backend.eventsourcing.journals

import fairshare.backend.Globals
import fairshare.backend.eventsourcing._

import doobie.imports._
import doobie.syntax._

import scalaz.concurrent.Task
import scalaz.stream.Process


class PostgresJournal[E](implicit ev: Atom[E]) extends Journal[E] {
  override def readAll(from: Index, to: Index): Process[Task, Fact[E]] = ???

  override def readSubject(key: Subject): Process[Task, Fact[E]] = ???

  override def write(key: Subject, revision: Revision, data: E): Task[WriteResult[E]] = {
    val xa = DriverManagerTransactor[Task](
      Globals.dbCfg.getString("driver"),
      Globals.dbCfg.getString("jdbc"),
      Globals.dbCfg.getString("name"),
      Globals.dbCfg.getString("pswd")
    )
    val q =
      sql"""
         |SELECT
         |  index, subject, revision, event_data, created_at
         |FROM events
         |WHERE subject = '$key' AND revision = '$revision'
      """.query[Fact[E]]
        .list
        .transact(xa)
        .run
    q match {
      case List() => Task(WriteFailure(s"Stream element ($key, $revision) already exists"))
      case _ => Task(WriteFailure(s"Function is not finished yet"))
    }
  }
}
