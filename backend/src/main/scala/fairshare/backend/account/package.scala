package fairshare.backend

package object account {
  type Money = BigDecimal

  object Money {
    def apply(v: Int): Money = BigDecimal(v)

    def apply(v: Double): Money = BigDecimal(v)
  }
}
