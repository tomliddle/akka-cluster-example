package models.persist

import akka.actor.SupervisorStrategy.{Restart, Resume}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import common.AppConfiguration
import models.persist.ClusterSingleton.CreatePersistActor
import play.api.Application
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object ClusterSingleton {

  val Name = "clustersingleton"

  def props(
             ws: WSClient,
             app: Application,
             config: AppConfiguration,
             ec: ExecutionContext): Props =
    Props(new ClusterSingleton(ws, app, config, ec))

  case object CreatePersistActor

}
/**
  * The cluster singleton
  * It doesn't do much as we cannot have this actor have persistence and crash as it won't recover.
  * This becomes the supervisor and can force a restart
  */
class ClusterSingleton (ws: WSClient,
                        app: Application,
                        config: AppConfiguration,
                        implicit val ec: ExecutionContext
                       ) extends Actor with ActorLogging with InjectedActorSupport {

  /**
    * Supervisor strategy
    */
  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy(loggingEnabled = false) {
    case ex : Exception =>
      // Get cause shows a better reason for error
      log.error(s"exception thrown ${ex.getCause.getMessage}")
      Resume

    // For errors we restart the actor
    case e: Error =>
      log.error(s"exception thrown ${e.getCause.getMessage}")
      Restart
  }


  private def createPersistActor: ActorRef = {
    val rm = context.system.actorOf(PersistActor.props(ws, app, config, ec)
      .withDispatcher("persistent-mailbox"))

    context.watch(rm)
  }

  private var persistActor = createPersistActor



  override def receive: Receive = {
    case Terminated(child) =>
      log.warning(s"termination notice received for [${child.path.name}]")
      context.system.scheduler.scheduleOnce(5.seconds, self, CreatePersistActor)

    case CreatePersistActor =>
      persistActor = createPersistActor

    // Forward all messages
    case x =>
      persistActor forward x
  }
}