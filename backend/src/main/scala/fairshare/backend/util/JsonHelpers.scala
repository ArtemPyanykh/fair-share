package fairshare.backend.util

import argonaut.EncodeJson

import scalaz.Show
import argonaut.Argonaut._

object JsonHelpers {
  implicit def mapLikeEncode[K, V](implicit kShow: Show[K], e: EncodeJson[V]): EncodeJson[Map[K, V]] =
    EncodeJson(x => jObjectAssocList(
      x.toList map {
        case (k, v) => (kShow.shows(k), e(v))
      }
    ))
}
