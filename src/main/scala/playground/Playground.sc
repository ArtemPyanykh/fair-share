import java.util.UUID

import project.ProjectCreated

//import java.util.UUID
//
//import project._
import argonaut._
import Argonaut._
import util.ids._
//import cqrs.typeclass.StringCodec.ops._
//
//implicit val projectCreatedCodec = CodecJson[ProjectCreated](
//  pc => Json("id" := pc.id.encode, "name" := pc.name),
//  cursor => {
//    for {
//      id <- (cursor --\ "id").as[String]
//      name <- (cursor --\ "name").as[String]
//    } yield ProjectCreated(ProjectId(UUID.fromString(id)), name)
//  }
//)
//
//val pc = ProjectCreated(ProjectId(UUID.randomUUID()), "YOLO-LO")
//
//val encoded = pc.asJson.spaces2
//val decoded = encoded.decodeEither[ProjectCreated]
//val decodedShit = Parse.parse("{some: shit}")
//case class Address(street: String, city: String)
//implicit val addressCoded = casecodec2(Address.apply, Address.unapply)("street", "city")
//Address("some street", "any city").asJson.spaces2
//sealed trait Father
//case class A0(a: Int) extends Father
//case class BS(b: String, s: Address) extends Father
//def taggedDecode[A](tag: String, c: HCursor, decoder: DecodeJson[A]): DecodeResult[A] = {
//  (c --\ tag).hcursor.fold(
//    DecodeResult.fail[A](s"Failed parse: expected $tag.", c.history)
//  )(decoder.decode)
//}
//def taggedEncode[A](a: A)(tag: String, encode: EncodeJson[A]): Json = {
//  Json(tag := encode.encode(a))
//}
//
//implicit val fatherCodec = CodecJson[Father](
//  {
//    case a @ A0(_) => taggedEncode[A0](a)("A", jencode1L(A0.unapply)("a"))
//    case BS(b, s) => Json("BS" := Json("b" := b, "s" := s))
//  },
//  cursor => {
//    taggedDecode[Father]("A", cursor, jdecode1L[Int, Father](A0.apply)("a")) |||
//    taggedDecode[Father]("BS", cursor, jdecode2L[String, Address, Father](BS.apply)("b", "s"))
//  }
//)
//
//val a0: Father = A0(5)
//
//val a0s = a0.asJson.spaces2
//
//val bs: Father = BS("hoy", Address("sweet", "city"))
//
//val bss = bs.asJson.spaces2
//
//bss.decodeEither[Father]
//a0s.decodeEither[Father]
//classOf[Project].getTypeName
//classOf[Project].getName
//classOf[Project].getCanonicalName
//classOf[Project].getSimpleName
//
//case class WithTP[T](a: T)
//classOf[WithTP[Int]].getTypeName
//classOf[WithTP[Int]].getName
//
//import argonaut.Shapeless._
//
//val sEncode = EncodeJson.of[Father]
//val sDecode = DecodeJson.of[Father]
//
//Parse.decodeEither(sEncode.encode(bs).spaces2)(sDecode)
//
//Parse.decodeEither[Father]("this: is_shit")(sDecode)
//
//val pce = EncodeJson.of[ProjectCreated]
//
//pce.encode(ProjectCreated(ProjectId(UUID.randomUUID()), "WAT"))
val pid = ProjectId(UUID.randomUUID())
val p = ProjectCreated(pid, "WAT")

p.asJson.nospaces.decodeEither[ProjectCreated]
