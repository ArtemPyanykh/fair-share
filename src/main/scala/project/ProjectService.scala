package project

import es.Version
import project.model.ProjectStatus
import project.operations.ProjectOperations
import util.ids._
import util.types._

import scalaz.Reader
import scalaz.syntax.either._

trait ProjectService {
  val commands: ProjectOperations

  def create(id: ProjectId, name: String): Reader[ProjectRepo, ValidS[Int]] = Reader { repo =>
    repo.get(id).swap.fold(
      _ => s"Project with id = $id already exists.".left,
      _ => {
        val project = commands.create(id, name).run(())

        for {
          project <- project
          toRepo <- repo.store(project)
        } yield toRepo
      }
    )
  }

  def modifyName(id: ProjectId, name: String, version: Version): Reader[ProjectRepo, ValidS[Int]] = Reader { repo =>
    for {
      project <- repo.get(id)
      changed <- (commands.checkAgainstVersion(version) andThen commands.modifyName(name)).run(project)
      toRepo <- repo.store(changed)
    } yield toRepo
  }

  def modifyStatus(id: ProjectId, status: ProjectStatus, version: Version): Reader[ProjectRepo, ValidS[Int]] = Reader { repo =>
    for {
      project <- repo.get(id)
      changed <- (commands.checkAgainstVersion(version) andThen commands.modifyStatus(status)).run(project)
      toRepo <- repo.store(changed)
    } yield toRepo
  }
}
