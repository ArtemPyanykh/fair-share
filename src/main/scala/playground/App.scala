package playground

import java.util.UUID

import org.http4s.HttpService
import org.http4s.server.blaze.BlazeBuilder

import scala.collection.mutable

object App {
  import org.http4s.dsl._

  val storage = mutable.ArrayBuffer[StreamElement[ProjectEvent]]()

  val streamReader = new InMemoryStreamReader[ProjectEvent](storage)

  val streamWriter = new InMemoryStreamWriter[ProjectEvent](storage)

  val eventStore = EventStore(streamReader, streamWriter)

  val pcs = new ProjectCommandService

  val pvs = new ProjectViewService(streamReader)

  val example = HttpService {
    case req @ POST -> Root / "create" =>
      val name = req.params.get("name")
      val motto = req.params.get("motto")

      (name, motto) match {
        case (Some(n), Some(m)) =>
          val uuid = UUID.randomUUID()
          val result = pcs.create(Project.Id(uuid), n, m)(eventStore)
          result.fold(
            error => InternalServerError(s"Something went wrong buddy: $error"),
            el => Ok(el.key.key)
          )
        case _ =>
          BadRequest("Not enough parameters")
      }

    case req @ POST -> Root / "rename" =>
      val name = req.params.get("name")
      name match {
        case Some(n) =>
          Ok(s"I got: $name")
        case None =>
          BadRequest("Not enough parameters")
      }

    case req @ GET -> Root / "list" =>
      Ok(pvs.list.map {
        case (id, view) =>
          s"This is: ${id.uuid.toString}, ${view.name}"
      }.mkString("\n"))
  }

  def main(args: Array[String]): Unit = {
    val port = 8080

    BlazeBuilder.bindHttp(port)
      .mountService(example)
      .run.awaitShutdown()
  }

}
