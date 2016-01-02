package util

import project.model.Project

trait Ids {
  type ProjectId = Id[Project]
  val ProjectId = Id.apply[Project] _
}
