package models.persist

import akka.actor.{Actor, ActorLogging, PoisonPill}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import akka.util.Timeout
import common.AppConfiguration
import javax.inject.{Inject, Singleton}
import play.api.Application
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

object ClusterSingletonSupervisor {
  val Name = "receiver"
}

/**
  *
  */
@Singleton
class ClusterSingletonSupervisor @Inject()(
                                            ws: WSClient,
                                            app: Application,
                                            config: AppConfiguration)
                                          (implicit val ec: ExecutionContext)
                                       extends Actor with ActorLogging with InjectedActorSupport {

  private implicit val timeout: Timeout = config.timeout

  // Create the cluster singleton manager
 context.system.actorOf(
    ClusterSingletonManager.props(
      singletonProps = ClusterSingleton.props(ws, app, config, ec),
      terminationMessage = PoisonPill,
      settings = ClusterSingletonManagerSettings(context.system)),
    name = ClusterSingleton.Name)

  // Create the proxy to the cluster singleton
  private val clusterSupervisor = context.system.actorOf(
    ClusterSingletonProxy.props(
      singletonManagerPath = s"/user/${ClusterSingleton.Name}",
      settings = ClusterSingletonProxySettings(context.system)),
    name = "clustersingletonproxy")


  override def receive: Receive = {

    // Forward all messages to cluster singleton
    case x =>
      clusterSupervisor forward x
  }
}