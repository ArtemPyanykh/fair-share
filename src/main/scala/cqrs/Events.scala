package cqrs

case class Events[C, M](creation: C, modifications: Vector[M]) {
  def withEvent(event: M): Events[C, M] = copy(modifications = modifications :+ event)

  def mapC[CC](f: C => CC): Events[CC, M] = {
    val newC = f(creation)
    copy(creation = newC)
  }

  def mapM[MM](f: M => MM): Events[C, MM] = {
    val newM = modifications.map(f)
    copy(modifications = newM)
  }

  def mapCM[CC, MM](f: C => CC, g: M => MM): Events[CC, MM] = mapC(f).mapM(g)
}
