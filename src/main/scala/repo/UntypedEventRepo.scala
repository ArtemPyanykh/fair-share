package repo

import scalaz.\/

trait UntypedEventRepo {
  def storeAll(data: Vector[UntypedEventData]): Throwable \/ Unit

  def getByKey(tag: String, id: String): Throwable \/ Vector[UntypedEventData]
}
