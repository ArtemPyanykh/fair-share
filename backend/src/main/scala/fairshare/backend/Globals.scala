package fairshare.backend

import java.util.concurrent.Executors
import scala.concurrent.duration._

/**
  * Created by tomas on 13/03/16.
  */
object Globals {
  lazy val executor = Executors.newScheduledThreadPool(1)
  val pollingFrequency = 100.millis
}
