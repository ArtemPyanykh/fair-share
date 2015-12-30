package cqrs

case class Version(v: Int) extends AnyVal with Ordered[Version] {
  def compare(that: Version): Int = this.v - that.v

  def next: Version = copy(v + 1)

  def prev: Version = copy(v - 1)

  def isZero: Boolean = this == Version.zero
}

object Version {
  def zero: Version = Version(0)
  def one: Version = Version(1)
}
