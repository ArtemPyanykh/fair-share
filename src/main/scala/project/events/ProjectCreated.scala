package project.events

import argonaut._
import argonaut.Argonaut._
import es.typeclass.EventC
import project.model.{ ProjectStatus, Project }
import util.ids._
import util.json._

case class ProjectCreated(id: ProjectId, name: String)

object ProjectCreated extends ProjectCreatedCodecs {
  implicit val eventC: EventC[Project, ProjectCreated] = EventC {
    case ProjectCreated(id, name) => Project(id = id, name = name, ProjectStatus.Open)
  }
}

trait ProjectCreatedCodecs {
  implicit val encodeJson: EncodeJson[ProjectCreated] = taggedEncode("ProjectCreated") { ev =>
    Json("id" := ev.id, "name" := ev.name)
  }

  implicit val decodeJson: DecodeJson[ProjectCreated] = taggedDecode("ProjectCreated") { hc =>
    for {
      id <- (hc --\ "id").as[ProjectId]
      name <- (hc --\ "name").as[String]
    } yield ProjectCreated(id, name)
  }

  implicit val codecJson: CodecJson[ProjectCreated] = CodecJson.derived(encodeJson, decodeJson)
}
