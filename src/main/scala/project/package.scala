import cqrs.Aggregate

package object project {
  type ProjectAggregate = Aggregate[Project, ProjectCreated, ProjectModified]
}
