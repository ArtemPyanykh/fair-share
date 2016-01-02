package project.events

import argonaut._
import argonaut.Argonaut._
import es.typeclass.EventM
import project.model.{ ProjectStatus, Project }
import util.json._

sealed trait ProjectModified

case class ProjectNameModified(name: String) extends ProjectModified

case class ProjectStatusModified(status: ProjectStatus) extends ProjectModified

object ProjectModified extends ProjectModifiedCodecs {
  implicit val eventM: EventM[Project, ProjectModified] = EventM { model =>
    {
      case ProjectNameModified(name) => model.copy(name = name)
      case ProjectStatusModified(status) => model.copy(status = status)
    }
  }
}

trait ProjectModifiedCodecs {
  val nameModifiedTag = "ProjectNameModified"
  val statusModifiedTag = "ProjectStatusModified"

  implicit val encodeJson: EncodeJson[ProjectModified] = EncodeJson {
    case a @ ProjectNameModified(_) => taggedJson(nameModifiedTag) {
      Json("name" := a.name)
    }
    case a @ ProjectStatusModified(_) => taggedJson(nameModifiedTag) {
      Json("status" := a.status)
    }
  }

  implicit val decodeJson: DecodeJson[ProjectModified] =
    taggedDecode[ProjectModified](nameModifiedTag) { hc =>
      for {
        name <- (hc --\ "name").as[String]
      } yield ProjectNameModified(name)
    } |||
      taggedDecode[ProjectModified](statusModifiedTag) { hc =>
        for {
          status <- (hc --\ "status").as[ProjectStatus]
        } yield ProjectStatusModified(status)
      }

  implicit val codecJson: CodecJson[ProjectModified] = CodecJson.derived(encodeJson, decodeJson)
}
