package project

import cqrs.{ Aggregate, Version }
import repo.AggregateRepo
import util.ids._
import util.types._

import scalaz.Reader
import scalaz.syntax.either._

trait ProjectService {
  val commands: ProjectCommands[Aggregate[Project, ProjectCreated, ProjectModified]]
  type ProjectRepo = AggregateRepo[Project, Aggregate[?, ProjectCreated, ProjectModified]]

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

  def modifyName(id: ProjectId, name: String, version: Version): Reader[ProjectRepo, ValidS[Unit]] = Reader { repo =>
    for {
      project <- repo.getAggregateV(id)
      changed <- (commands.checkAgainstVersion(version) andThen commands.modifyName(name)).run(project)
      toRepo <- repo.storeAggregateV(changed)
    } yield toRepo
  }

  def modifyStatus(id: ProjectId, status: ProjectStatus, version: Version): Reader[ProjectRepo, ValidS[Unit]] = Reader { repo =>
    for {
      project <- repo.getAggregateV(id)
      changed <- (commands.checkAgainstVersion(version) andThen commands.modifyStatus(status)).run(project)
      toRepo <- repo.storeAggregateV(changed)
    } yield toRepo
  }
}
