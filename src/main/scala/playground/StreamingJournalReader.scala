package playground

import java.util.concurrent.{ Executors, ScheduledExecutorService }

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scalaz.concurrent.{ Strategy, Task }
import scalaz.stream._

class StreamingJournalReader[E](
    underlying: Journal[E],
    updateFrequency: FiniteDuration,
    bufferSize: Int = StreamingJournalReader.DefaultBufferSize
)(implicit executor: ScheduledExecutorService) {
  var alreadyReadEls: Int = 0
  val bus = async.boundedQueue[Fact[E]](bufferSize)(Strategy.Executor(executor))

  def listenToJournal: Process[Task, Unit] = getUnreadRetry(bufferSize, updateFrequency).to(bus.enqueue)

  def readUpdating: Process[Task, Fact[E]] = bus.dequeue

  private[this] def queryUnderlying(from: Int, limit: Int): Task[Vector[Fact[E]]] =
    underlying.readAll(FactNumber(alreadyReadEls), FactNumber(alreadyReadEls + limit - 1)).runLog

  private[this] def getUnreadRetry(limit: Int, retryFreq: FiniteDuration): Process[Task, Fact[E]] =
    Process.await(queryUnderlying(alreadyReadEls, limit)) {
      got =>
        if (got.nonEmpty) {
          alreadyReadEls += got.length
          Process.emitAll(got).toSource ++ getUnreadRetry(limit, retryFreq)
        } else {
          time.awakeEvery(retryFreq)(Strategy.Sequential, executor).once
            .flatMap { _ =>
              getUnreadRetry(limit, retryFreq)
            }
        }
    }
}

object StreamingJournalReader {
  val DefaultBufferSize = 10
}

object JournalReaderTest {
  val scheduledEx = Executors.newSingleThreadScheduledExecutor()
  //  val scheduledEx = Executors.newScheduledThreadPool(4)

  val consoleSink = sink.lift[Task, String](
    el => Task.delay {
      println(s"Got $el")
    }
  )

  val buffer = new InMemoryJournal[Int](ArrayBuffer())

  def stop(): Unit = {
    scheduledEx.shutdown()
  }

  val streamingReader = new StreamingJournalReader(buffer, 2.seconds)(scheduledEx)

  val leftPipeline = streamingReader.readUpdating
  val rightPipeline = streamingReader.readUpdating

  val merged: Process[Task, String] =
    (leftPipeline map { el => s"left: $el" }).merge(rightPipeline map { el => s"right: $el" })

  val pipeline = merged.to(consoleSink)

  def startPipeline(): Unit = {
    streamingReader.listenToJournal.run.runAsync(_ => ())
    pipeline.run.runAsync(_ => ())
  }
}
