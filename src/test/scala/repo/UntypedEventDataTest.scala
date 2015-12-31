package repo

import java.util.UUID

import org.scalatest.{ Matchers, WordSpec }
import argonaut._
import Argonaut._
import project.ProjectCreated
import util.ids._
import cqrs.StringCodec.ops._

class UntypedEventDataTest extends WordSpec with Matchers {

  "UntypedEventDataTest" should {
    "build from EventData" in {
    }
  }
}
