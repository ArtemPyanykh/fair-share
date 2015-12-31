package project

import cqrs.typeclass.Entity
import util.Id
import util.ids._

case class Project(
  id: ProjectId,
  name: String,
  status: ProjectStatus
)

sealed trait ProjectStatus extends Product with Serializable

object ProjectStatus {

  case object Open extends ProjectStatus

  case object Removed extends ProjectStatus

  case object Finished extends ProjectStatus

}

object Project {
  implicit val entity: Entity[Project] = new Entity[Project] {
    def id(a: Project): Id[Project] = a.id
  }
}

