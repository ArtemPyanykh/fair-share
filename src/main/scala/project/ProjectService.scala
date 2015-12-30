package project

import cqrs.{Aggregate, Version}
import repo.AggregateRepo
import util.ids._
import util.types._

import scalaz.Reader
import scalaz.syntax.either._

trait ProjectService[AA <: Aggregate[Project]] {
  val commands: ProjectCommands[AA]
  type ProjectRepo = AggregateRepo[Project, AA]

  def create(id: ProjectId, name: String): Reader[ProjectRepo, ValidS[Unit]] = Reader { repo =>
    repo.getAggregateV(id).swap.fold(
      _ => s"Project with id = $id already exists.".left,
      _ => {
        val project = commands.create(id, name).run(())

        for {
          project <- project
          toRepo <- repo.storeAggregateV(project)
        } yield toRepo
      }
    )
  }

  def changeName(id: ProjectId, name: String, version: Version): Reader[ProjectRepo, ValidS[Unit]] = Reader { repo =>
    for {
      project <- repo.getAggregateV(id)
      changed <- (commands.checkAgainstVersion(version) andThen commands.changeName(name)).run(project)
      toRepo <- repo.storeAggregateV(changed)
    } yield toRepo
  }

  def changeStatus(id: ProjectId, status: ProjectStatus, version: Version): Reader[ProjectRepo, ValidS[Unit]] = Reader { repo =>
    for {
      project <- repo.getAggregateV(id)
      changed <- (commands.checkAgainstVersion(version) andThen commands.changeStatus(status)).run(project)
      toRepo <- repo.storeAggregateV(changed)
    } yield toRepo
  }
}
