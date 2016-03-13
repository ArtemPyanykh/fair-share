package fairshare.backend.eventsourcing.journals

import java.util.concurrent.ScheduledExecutorService

import fairshare.backend.eventsourcing._

import scala.concurrent.duration._
import scalaz.concurrent.{ Strategy, Task }
import scalaz.stream._

class PollingJournalReader[E](
    underlying: Journal[E],
    pollingFrequency: FiniteDuration,
    bufferSize: Int = PollingJournalReader.DefaultBufferSize
)(implicit executor: ScheduledExecutorService) {
  var alreadyReadEls: Int = 0
  val bus = async.boundedQueue[Fact[E]](bufferSize)(Strategy.Executor(executor))

  def listenUpdates: Process[Task, Unit] = getUnreadRetry(bufferSize, pollingFrequency).to(bus.enqueue)

  def readUpdates: Process[Task, Fact[E]] = bus.dequeue

  private[this] def queryUnderlying(from: Int, limit: Int): Task[Vector[Fact[E]]] =
    underlying.readAll(Index(alreadyReadEls), Index(alreadyReadEls + limit - 1)).runLog

  private[this] def getUnreadRetry(limit: Int, retryFreq: FiniteDuration): Process[Task, Fact[E]] =
    Process.await(queryUnderlying(alreadyReadEls, limit)) {
      got =>
        if (got.nonEmpty) {
          alreadyReadEls += got.length
          Process.emitAll(got).toSource ++ getUnreadRetry(limit, retryFreq)
        } else {
          time.sleep(retryFreq)(Strategy.Sequential, executor) ++ getUnreadRetry(limit, retryFreq)
        }
    }
}

object PollingJournalReader {
  val DefaultBufferSize = 10
}
