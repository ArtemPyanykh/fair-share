package fairshare.backend.account

sealed trait Side {
  def contra: Side = this match {
    case Debit => Credit
    case Credit => Debit
  }
}

case object Debit extends Side

case object Credit extends Side
