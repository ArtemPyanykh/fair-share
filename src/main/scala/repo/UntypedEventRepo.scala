package repo

import scalaz.\/

trait UntypedEventRepo {
  def storeAll(data: Vector[UntypedEventData]): Throwable \/ Int

  def getBy(tag: String, id: String): Throwable \/ Vector[UntypedEventData]
}
