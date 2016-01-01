package project

import cqrs.typeclass.StringCodec
import util.json._
import util.codecs._
import util.ids._
import argonaut._
import Argonaut._
import cqrs.typeclass.StringCodec.ops._

trait ProjectCreatedCodecs {
  implicit val jsonEncode = taggedEncode[ProjectCreated]("ProjectCreated") {
    jencode2L[ProjectCreated, ProjectId, String](x => (x.id, x.name))("id", "name")
  }

  implicit val jsonDecode = taggedDecode[ProjectCreated]("ProjectCreated") {
    jdecode2L(ProjectCreated.apply)("id", "name")
  }

  implicit val codec = fromJsonCodec(jsonEncode, jsonDecode)
}

trait ProjectModifiedCodecs {
  implicit val jsonEncode: EncodeJson[ProjectModified] = EncodeJson {
    case x @ ProjectNameModified(_) => taggedEncode("ProjectNameModified") {
      jencode1L[ProjectNameModified, String](_.name)("name")
    }.encode(x)

    case x @ ProjectStatusModified(_) => taggedEncode("ProjectStatusModified") {
      jencode1L[ProjectStatusModified, String](_.status.encode)("status")
    }.encode(x)
  }

  implicit val jsonDecode: DecodeJson[ProjectModified] = taggedDecode[ProjectModified]("ProjectNameModified") {
    DecodeJson { cursor =>
      for {
        name <- (cursor --\ "name").as[String]
      } yield ProjectNameModified(name)
    }
  } ||| taggedDecode[ProjectModified]("ProjectStatusModified") {
    DecodeJson { cursor =>
      (cursor --\ "status").as[String].flatMap { statusS =>
        val codec = implicitly[StringCodec[ProjectStatus]]
        val status = codec.decode(statusS)
        status.fold(
          _ => DecodeResult.fail[ProjectModified]("Wrong status", cursor.history),
          s => DecodeResult.ok[ProjectModified](ProjectStatusModified(s))
        )
      }
    }
  }

  implicit val codec = fromJsonCodec(jsonEncode, jsonDecode)
}
