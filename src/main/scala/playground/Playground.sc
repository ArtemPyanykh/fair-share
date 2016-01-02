import java.util.UUID
import argonaut._
import Argonaut._
import project.{ProjectNameModified, ProjectModified}
import util.ids._
val pid = ProjectId(UUID.randomUUID())
case class A(a: ProjectId)
//Shapeless.mkEncodeJson[A].encode(A(ProjectId(UUID.randomUUID())))
//val enc = EncodeJson.of[ProjectModified]
//val ev: ProjectModified = ProjectNameModified("hello")

//ev.asJson.nospaces.decodeEither[ProjectModified]

pid.asJson
