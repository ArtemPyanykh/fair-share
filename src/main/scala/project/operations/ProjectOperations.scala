package project.operations

import es._
import project._
import project.model.ProjectStatus
import util.ids._
import util.types._

trait ProjectOperations {
  def create(id: ProjectId, name: String): CreateValidS[ProjectAggregate]

  def modifyName(name: String): ChangeValidS[ProjectAggregate]

  def modifyStatus(status: ProjectStatus): ChangeValidS[ProjectAggregate]

  def checkAgainstVersion(version: Version): ChangeValidS[ProjectAggregate]
}

