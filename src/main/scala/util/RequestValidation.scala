package util

import org.http4s.Request

import scala.util.{ Failure, Success, Try }
import scalaz.{ ValidationNel, Validation }
import scalaz.syntax.validation._
import scalaz.syntax.either._
import scalaz.syntax.apply._

object RequestValidation {
  def any: Option[String] => Validation[String, Option[String]] = opt => opt.success

  def string: Option[String] => Validation[String, String] = {
    case Some(obj) => obj.success
    case None => "should be present".failure
  }

  def nonEmpty: Validation[String, String] => Validation[String, String] = stringValidation =>
    stringValidation.disjunction.flatMap { str => //scalastyle:off
      if (str.nonEmpty) str.right else "should not be empty".left
    }.validation //scalastyle:on

  def nonEmptyString: Option[String] => Validation[String, String] = string.andThen(nonEmpty)

  def number: Validation[String, String] => Validation[String, Int] = stringValidation => {
    stringValidation.disjunction.flatMap[String, Int] { str =>
      Try(str.toInt) match {
        case Success(i) => i.right
        case Failure(ex) => s"$str couldn't be parsed as a number".left
      }
    }.validation
  }

  def keyed[A](key: String): Validation[String, A] => Validation[String, A] = Validation =>
    Validation.leftMap(originalMessage => s"Error: $key $originalMessage")

  def bindFromRequest[A1, B](request: Request, mapping: (String, Option[String] => Validation[String, A1]))(f: A1 => B): ValidationNel[String, B] = {
    val v = keyed(mapping._1)(mapping._2(request.params.get(mapping._1)).map(f))
    v.toValidationNel
  }

  def bindFromRequest[A1, A2, B](
    request: Request,
    mapping1: (String, Option[String] => Validation[String, A1]),
    mapping2: (String, Option[String] => Validation[String, A2])
  )(f: (A1, A2) => B): ValidationNel[String, B] = {
    val v1 = keyed(mapping1._1)(mapping1._2(request.params.get(mapping1._1))).toValidationNel
    val v2 = keyed(mapping2._1)(mapping2._2(request.params.get(mapping2._1))).toValidationNel

    (v1 |@| v2)(f)
  }

  def bindFromRequest[A1, A2, A3, B](
    request: Request,
    mapping1: (String, Option[String] => Validation[String, A1]),
    mapping2: (String, Option[String] => Validation[String, A2]),
    mapping3: (String, Option[String] => Validation[String, A3])
  )(f: (A1, A2, A3) => B): ValidationNel[String, B] = {
    val v1 = keyed(mapping1._1)(mapping1._2(request.params.get(mapping1._1))).toValidationNel
    val v2 = keyed(mapping2._1)(mapping2._2(request.params.get(mapping2._1))).toValidationNel
    val v3 = keyed(mapping3._1)(mapping3._2(request.params.get(mapping3._1))).toValidationNel

    (v1 |@| v2 |@| v3)(f)
  }

}
