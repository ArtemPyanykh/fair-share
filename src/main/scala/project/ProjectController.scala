package project

import java.util.UUID

import argonaut.Argonaut._
import argonaut.Json
import org.http4s.argonaut._
import org.http4s.dsl._
import org.http4s.{ HttpService, Request, Response }
import util.RequestValidation._
import util.JsonHelpers._

import scalaz.concurrent.Task

class ProjectController(commandHandler: ProjectCommandHandler, queryHandler: ProjectQueryHandler) {
  def create(request: Request): Task[Response] = bindFromRequest(
    request,
    "name" -> nonEmptyString,
    "motto" -> nonEmptyString
  ) { (name, motto) =>
      val id = ProjectId(UUID.randomUUID())

      commandHandler.create(id, name, motto).flatMap(
        _.fold(
          error => InternalServerError(error),
          fact => Ok(Json("id" := fact.subject))
        )
      )
    }.fold(
      errs => BadRequest(errs.asJson),
      success => success
    )

  def index(request: Request): Task[Response] = {
    val projects = queryHandler.getAll

    Ok(projects.asJson)
  }

}

object ProjectController {
  def service(controller: ProjectController): HttpService = HttpService {
    case req @ GET -> Root / "index" => controller.index(req)

    case req @ POST -> Root / "create" => controller.create(req)
  }

}
