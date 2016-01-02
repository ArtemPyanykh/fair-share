package project.model

import es.typeclass.{ Entity, Tag }
import util.Id
import util.ids._

case class Project(
  id: ProjectId,
  name: String,
  status: ProjectStatus
)

object Project {
  implicit val entity: Entity[Project] = new Entity[Project] {
    def id(a: Project): Id[Project] = a.id
  }

  implicit val tagged: Tag[Id[Project]] = new Tag[Id[Project]] {
    def tag(a: Id[Project]): String = "Project"
  }
}

