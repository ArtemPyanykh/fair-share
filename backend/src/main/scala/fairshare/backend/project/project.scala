package fairshare.backend.project

import java.util.UUID

import scalaz.Show
import fairshare.backend.util.Iso

case class Project(id: ProjectId, name: String)

case class ProjectId(uuid: UUID) extends AnyVal

object ProjectId {
  implicit val uuidIso = Iso.build[ProjectId, UUID](p => p.uuid, id => ProjectId(id))

  implicit val show: Show[ProjectId] = Show.show(_.uuid.toString)
}
