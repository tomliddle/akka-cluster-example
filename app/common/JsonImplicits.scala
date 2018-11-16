package common

import models.persist.PersistActor._
import play.api.libs.json._

object JsonImplicits {
  implicit val testMsgFmt: OFormat[TestMessage] = Json.format[TestMessage]
}


object OrderingImplicits {

  implicit val testOrdering: Ordering[TestMessage] =  Ordering.by { r => r.id }

}
