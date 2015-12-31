package project

import java.util.UUID

import cqrs._
import org.scalatest.{ FunSpec, Matchers }
import repo._
import util.ids._

class ProjectServiceTest extends FunSpec with Matchers {
  describe("ProjectServiceTest") {
    def getRepo = new EventAggregateRepo[Project, ProjectCreated, ProjectModified](new InMemoryEventRepo)

    val projectService = new ProjectService {
      val commands = ProjectCommandsInterpreter.EventSourcedProjectCommands
    }

    it("should create") {
      val repo = getRepo

      val testId = ProjectId(UUID.randomUUID())

      projectService.create(testId, "POC").run(repo)
      projectService.modifyName(testId, "POC2", Version.one).run(repo)

      val fromRepo = repo.getAggregate(testId)

      fromRepo.fold(
        e => fail(e),
        pOpt => pOpt shouldEqual Some(
          Aggregate(
            Version(2), Project(testId, "POC2", ProjectStatus.Open), ProjectCreated(testId, "POC"),
            Vector(ProjectNameModified("POC2"))
          )
        )
      )
    }
  }
}
