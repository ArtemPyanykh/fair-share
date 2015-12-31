package cqrs

case class Version(value: Int) extends AnyVal with Ordered[Version] {
  def compare(that: Version): Int = this.value - that.value

  def next: Version = copy(value + 1)

  def prev: Version = copy(value - 1)

  def isZero: Boolean = this == Version.zero
}

object Version {
  def zero: Version = Version(0)
  def one: Version = Version(1)
}
