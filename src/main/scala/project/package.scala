import es.Aggregate
import project.events.{ ProjectCreated, ProjectModified }
import project.model.Project

package object project {
  type ProjectAggregate = Aggregate[Project, ProjectCreated, ProjectModified]
}
