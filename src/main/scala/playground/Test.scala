package playground

import java.util.concurrent._
import java.util.concurrent.atomic.AtomicBoolean

import scala.collection.mutable
import scalaz.Sink
import scalaz.concurrent.{ Strategy, Task }
import scalaz.stream._

object Test {

  class StoreChecker(store: mutable.Buffer[Int]) {
    var elementsChecked = 0
    val maxBufferSize = 100
    var buffer: List[Int] = List()
    val attemptingTransmit = new AtomicBoolean(false)
    val bus: BlockingQueue[Int] = new ArrayBlockingQueue[Int](100)

    val storeStream: Process[Task, Int] = Process.repeatEval(Task.delay(bus.take()))

    def attemptTransmit(): Unit = {
      if (attemptingTransmit.compareAndSet(false, true)) {
        if (bus.remainingCapacity() > 0) {
          if (buffer.nonEmpty) {
            transmit()
          } else {
            replenishBuffer()
          }
        } else {
          println("The bus doesn't have capacity")
        }

        attemptingTransmit.set(false)
      } else {
        println("Transmit is in progress.")
      }
    }

    def transmit(): Unit = synchronized {
      buffer.take(bus.remainingCapacity()).foreach { el =>
        bus.add(el)
        buffer = buffer.tail
      }
    }

    def replenishBuffer(): Unit = synchronized {
      val (b, ec) = fetchNew
      buffer = b
      elementsChecked = ec
    }

    def fetchNew: (List[Int], Int) = {
      val newEls = store.slice(elementsChecked, elementsChecked + maxBufferSize).toList
      val newChecked = elementsChecked + newEls.size

      (newEls, newChecked)
    }
  }

  val executor = new ScheduledThreadPoolExecutor(2)

  val executor2 = java.util.concurrent.Executors.newSingleThreadExecutor()

  val store: mutable.Buffer[Int] = mutable.ArrayBuffer()

  val checker = new StoreChecker(store)

  val updates = checker.storeStream

  val consoleSink = sink.lift[Task, Int] { el => Task.delay { println(el) } }

  val wholeStreamTask = updates.to(consoleSink).run

  def runPeriodicChecks = executor.scheduleAtFixedRate(new Runnable {
    def run(): Unit = checker.attemptTransmit()
  }, 0, 100, TimeUnit.MILLISECONDS)

  def runStreamDrain = executor2.submit(new Callable[Unit] {
    def call(): Unit = wholeStreamTask.run
  })
}

object Test2 {
  val es = Executors.newSingleThreadExecutor()
  val es2 = Executors.newSingleThreadExecutor()

  val loggingStrategy = new Strategy {
    def apply[A](a: => A): () => A = {
      val future = es.submit(new Callable[A] {
        def call(): A = a
      })

      println("A task was submitted to executor")

      () => future.get()
    }
  }

  val loggingQueue = async.boundedQueue[Int](5)(loggingStrategy)
}
