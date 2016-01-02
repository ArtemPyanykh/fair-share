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

    it("should create") {
      val repo = getRepo

      val testId = ProjectId(UUID.randomUUID())

      projectService.create(testId, "POC").run(repo)
      projectService.modifyName(testId, "POC2", Version.one).run(repo)

      val fromRepo = repo.get(testId)

      fromRepo.fold(
        e => fail(e),
        a => {
          a.model shouldEqual Project(testId, "POC2", ProjectStatus.Open)
          a.persistedVersion shouldEqual Version(2)
          a.currentVersion shouldEqual Version(2)
        }
      )
    }
  }

  def getRepo: ProjectRepo = new ProjectRepo {
    val inner = new GeneralAggregateRepo[Project, ProjectCreated, ProjectModified](
      new GeneralEventRepo[Id[Project], ProjectCreated, ProjectModified](new InMemoryEventRepo)
    )

    def store(a: ProjectAggregate): ValidS[Unit] = inner.storeAggregate(a)

    def get(id: ProjectId): ValidS[ProjectAggregate] = inner.getAggregate(id)
  }
}
