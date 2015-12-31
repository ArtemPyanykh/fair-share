package project

import util.ids._
import util.types._

trait ProjectRepo {
  def store(a: ProjectAggregate): ValidS[Unit]

  def get(id: ProjectId): ValidS[ProjectAggregate]
}
