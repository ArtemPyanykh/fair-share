package project

import java.util.UUID

import cqrs._
import org.scalatest.{Matchers, FunSpec}
import repo._
import util.ids._

class ProjectServiceTest extends FunSpec with Matchers {
  describe("ProjectServiceTest") {
    def getRepo = new EventAggregateRepo[Project, ProjectCreated, ProjectChanged](new InMemoryEventRepo)

    val projectService = new ProjectService[EventAggregate[Project, ProjectCreated, ProjectChanged]] {
      val commands = ProjectCommandsInterpreter.EventSourcedProjectCommands
    }

    it("should create") {
      val repo = getRepo

      val testId = ProjectId(UUID.randomUUID())

      projectService.create(testId, "POC").run(repo)
      projectService.changeName(testId, "POC2", Version.one).run(repo)

      val fromRepo = repo.getAggregate(testId)

      fromRepo.fold(
        e => fail(e),
        pOpt => pOpt shouldEqual Some(
          EventAggregate(
            Version(2), Project(testId, "POC2", ProjectStatus.Open), ProjectCreated(testId, "POC"), Vector(ProjectNameChanged("POC2"))
          )
        )
      )
    }
  }
}
