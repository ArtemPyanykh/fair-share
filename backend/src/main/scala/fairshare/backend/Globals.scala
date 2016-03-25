package fairshare.backend

import java.util.concurrent.Executors
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object Globals {
  lazy val executor = Executors.newScheduledThreadPool(1)
  lazy val serverCfg = ConfigFactory.load("server")
  lazy val dbCfg = ConfigFactory.load("db")
  val pollingFrequency = 100.millis
}
