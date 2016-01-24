package project

import java.util.UUID

import es._
import org.scalatest.{ FunSpec, Matchers }
import project.events.{ ProjectModified, ProjectCreated }
import project.model.{ ProjectStatus, Project }
import project.operations.ProjectOperationsInterpreter
import repo._
import util.Id
import util.ids._
import util.types._

class ProjectServiceTest extends FunSpec with Matchers {
  describe("ProjectServiceTest") {
    val projectService = new ProjectService {
      val commands = ProjectOperationsInterpreter.ProjectOperations
    }

    it("should create, then modify name, then modify status") {
      val repo = getRepo

      val testId = ProjectId(UUID.randomUUID())

      projectService.create(testId, "TestProject").run(repo)
      var currVersion = Version.one
      projectService.modifyName(testId, "TestChangedName", currVersion).run(repo)

      var fromRepo = repo.get(testId)

      fromRepo.fold(
        e => fail(e),
        a => {
          a.model shouldEqual Project(testId, "TestChangedName", ProjectStatus.Open)
          a.persistedVersion shouldEqual Version(2)
          a.currentVersion shouldEqual Version(2)
        }
      )

      currVersion = currVersion.next
      projectService.modifyStatus(testId, ProjectStatus.Finished, currVersion).run(repo)

      fromRepo = repo.get(testId)

      fromRepo.fold(
        e => fail(e),
        a => {
          a.model shouldEqual Project(testId, "TestChangedName", ProjectStatus.Finished)
          a.persistedVersion shouldEqual Version(3)
          a.currentVersion shouldEqual Version(3)
        }
      )
    }
  }

  def getRepo: ProjectRepo = new ProjectRepo {
    val inner = new GeneralAggregateRepo[Project, ProjectCreated, ProjectModified](
      new GeneralEventRepo[Id[Project], ProjectCreated, ProjectModified](new PostgresRepo)
    )

    def store(a: ProjectAggregate): ValidS[Unit] = inner.storeAggregate(a)

    def get(id: ProjectId): ValidS[ProjectAggregate] = inner.getAggregate(id)
  }
}
