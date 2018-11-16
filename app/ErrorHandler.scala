import javax.inject.Singleton
import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent._
import ErrorHandler._

object ErrorHandler {
  implicit class BrightTALKHTTPStringExtensions(val s:String) {
    def toJson(statusCode: Int): JsObject = Json.obj("status" -> statusCode, "message" -> JsString(s))
  }
}

@Singleton
class ErrorHandler extends HttpErrorHandler with play.api.http.Status {

  private val log = Logger("errorhandler")

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      statusCode match {
        case BAD_REQUEST => BadRequest(message.toJson(statusCode))
        case UNAUTHORIZED => Unauthorized(message.toJson(statusCode))
        case FORBIDDEN => Forbidden(message.toJson(statusCode))
        case NOT_FOUND => NotFound(message.toJson(statusCode))
        case INTERNAL_SERVER_ERROR =>
          log.warn(message)
          InternalServerError(message.toJson(statusCode))
        case other =>
          log.warn(message)
          InternalServerError(message.toJson(statusCode))
      }
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    log.error(exception.getMessage)
    Future.successful(InternalServerError(exception.getMessage.toJson(500)))
  }
}
