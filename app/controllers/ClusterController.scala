package controllers

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import common.AppConfiguration
import javax.inject._
import models.persist.PersistActor._
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._



/**
 * Controller to handle incoming messages.
 */
@Singleton
class ClusterController @Inject()(
                                  cc: ControllerComponents,
                                  config: AppConfiguration,
                                  @Named("receiver") receiver: ActorRef
                              )(implicit ec: ExecutionContext, actorSystem: ActorSystem) extends AbstractController(cc) {

  private implicit val timeout: Timeout = Timeout(20 seconds)

  def test(id: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    (receiver ? TestMessage).mapTo[String].map { m =>
      Ok(m)
    }
  }
}
