package project.model

import argonaut._

sealed abstract class ProjectStatus(val name: String) extends Product with Serializable

object ProjectStatus {

  case object Open extends ProjectStatus("Open")

  case object Removed extends ProjectStatus("Removed")

  case object Finished extends ProjectStatus("Finished")

  implicit val encodeJson: EncodeJson[ProjectStatus] = EncodeJson.of[String].contramap(_.name)

  implicit val decodeJson: DecodeJson[ProjectStatus] = DecodeJson { hc =>
    hc.as[String].flatMap {
      case Open.name => DecodeResult.ok(Open)
      case Removed.name => DecodeResult.ok(Removed)
      case Finished.name => DecodeResult.ok(Finished)
      case x => DecodeResult.fail(s"Status $x doesn't exist", hc.history)
    }
  }

  implicit val codecJson: CodecJson[ProjectStatus] = CodecJson.derived(encodeJson, decodeJson)
}
