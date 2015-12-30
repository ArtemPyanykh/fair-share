import scalaz.Kleisli
import scalaz.syntax.bind._
import scalaz.std.option._

val iOpt: Option[Int] = Some(6)

val changeEven = Kleisli[Option, Int, Int] { num =>
  if (num % 2 == 0) Some(num / 2) else None
}

val fChangeEven: Int => Option[Int] = num => {
  if (num % 2 == 0) Some(num / 2) else None
}

iOpt >>= changeEven
