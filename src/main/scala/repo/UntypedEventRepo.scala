package repo

import scalaz.\/

trait UntypedEventRepo {
  def store(data: Vector[UntypedEventData]): Throwable \/ Unit

  def get(tag: String, id: String): Throwable \/ Vector[UntypedEventData]
}
