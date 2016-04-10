package fairshare.backend.eventsourcing.journals

import java.time.LocalDateTime

import fairshare.backend.Globals
import fairshare.backend.eventsourcing._

import doobie.imports._
import doobie.syntax._

import scalaz.Foldable
import scalaz.concurrent.Task
import scalaz.stream.Process


class PostgresJournal[E](implicit ev: Atom[E]) extends Journal[E] {
  val xa = DriverManagerTransactor[Task](
    Globals.dbCfg.getString("driver"),
    Globals.dbCfg.getString("jdbc"),
    Globals.dbCfg.getString("name"),
    Globals.dbCfg.getString("pswd")
  )

  override def readAll(from: Index, to: Index): Process[Task, Fact[E]] = ???

  override def readSubject(key: Subject): Process[Task, Fact[E]] = {
    // val q =
    sql"""
       |SELECT
       |  index, subject, revision, event_data, created_at
       |FROM events
       |WHERE subject = '$key'
    """.query[Fact[E]]
      .process
      .transact(xa)
//    xa.transP(q)
  }

  override def write(key: Subject, revision: Revision, data: E): Task[WriteResult[E]] = {

    val existing: List[Fact[E]] =
      sql"""
         |SELECT
         |  index, subject, revision, event_data, created_at
         |FROM events
         |WHERE subject = '$key' AND revision = '$revision'
      """.query[Fact[E]]  // (Int, String, Int, String, String)
        .process
        .list
        .transact(xa)
        .run
    existing.length match {
      case 0 => {
        val entriesCount: Int = sql"""SELECT COUNT(index) FROM events""".query[Int]
          .process
          .list
          .transact(xa)
          .run(0)
        val newFact = Fact(Index(entriesCount).next, key, revision, data, LocalDateTime.now())
        val writeQuery = s"""
           |INSERT into events (
           |  index, subject, revision, event_data, created_at
           |) values (?, ?, ?, ?, ?)
        """.stripMargin
        val q = Update[Fact[E]](writeQuery).updateMany(Vector(newFact)).transact(xa).run
        if (q == 1) {Task(WriteSuccess(newFact))}
        else {Task(WriteFailure(s"Something went wrong during writing"))}
      }
      case _ => Task(WriteFailure(s"Stream element ($key, $revision) already exists"))
    }
  }
}
