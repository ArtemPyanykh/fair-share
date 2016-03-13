package fairshare.backend.account

case class Account(
    cash: CashBalance.Balance,
    payable: PayableBalance.Balance,
    goods: GoodsBalance.Balance
) {
  def op(side: Side, label: LabelledBalance, money: Money): Account = label match {
    case x @ CashBalance =>
      copy(cash = cash.op(side, money))
    case x @ PayableBalance =>
      copy(payable = payable.op(side, money))
    case x @ GoodsBalance =>
      copy(goods = goods.op(side, money))
  }

  def give(money: Money): Account =
    op(Debit, PayableBalance, money)
      .op(Credit, CashBalance, money)

  def receive(money: Money): Account =
    op(Debit, CashBalance, money)
      .op(Credit, PayableBalance, money)

  def pay(check: Bill): Account =
    op(Debit, GoodsBalance, check.spent)
      .op(Credit, CashBalance, check.paid)
      .op(Credit, PayableBalance, check.spent - check.paid)
}

object Account {
  def empty: Account = Account(
    CashBalance.empty,
    PayableBalance.empty,
    GoodsBalance.empty
  )
}
