package project

import cqrs.typeclass.{ StringCodec, Entity }
import util.{ types, Id }
import util.ids._
import scalaz.syntax.either._

case class Project(
  id: ProjectId,
  name: String,
  status: ProjectStatus
)

sealed abstract class ProjectStatus(val name: String) extends Product with Serializable

object ProjectStatus {

  case object Open extends ProjectStatus("Open")

  case object Removed extends ProjectStatus("Removed")

  case object Finished extends ProjectStatus("Finished")

  implicit val stringCodec = new StringCodec[ProjectStatus] {
    def encode(e: ProjectStatus): String = e.name

    def decode(string: String): types.ValidS[ProjectStatus] = string match {
      case Open.name => Open.right
      case Removed.name => Removed.right
      case Finished.name => Finished.right
      case x => s"Unknown status $x".left
    }
  }

}

object Project {
  implicit val entity: Entity[Project] = new Entity[Project] {
    def id(a: Project): Id[Project] = a.id
  }
}

