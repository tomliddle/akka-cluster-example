package models.cluster

import akka.actor.{Actor, ActorLogging, Address, Timers}
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import common.AppConfiguration
import javax.inject.{Inject, Singleton}
import models.cluster.ClusterListener.{ClusterState, LogClusterState}

import scala.collection.immutable.SortedSet
import scala.concurrent.ExecutionContext

object ClusterListener {
  val Name = "clusterlistener"

  case class LogClusterState(force: Boolean = false)
  case class ClusterState(members: SortedSet[Member], leader: Option[Address], roleLeaderMap: Map[String, Option[Address]], seenBy: Set[Address], unreachable: Set[Member]) {
    override def toString: String = s"members:${members.flatMap(_.uniqueAddress.address.host)} leader:${leader.flatMap(_.host).getOrElse("")} seenBy:${seenBy.flatMap(_.host)} unrechable:$unreachable"
  }
}

@Singleton
class ClusterListener @Inject()(implicit ec: ExecutionContext, conf: AppConfiguration) extends Actor with ActorLogging with Timers {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember], classOf[LeaderChanged])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  private def clusterState: ClusterState = {
    val members: SortedSet[Member] = cluster.state.members
    val leader: Option[Address] = cluster.state.leader
    val roleLeaderMap: Map[String, Option[Address]] = cluster.state.roleLeaderMap
    val seenBy: Set[Address] = cluster.state.seenBy
    val unreachable: Set[Member] = cluster.state.unreachable

    ClusterState(members, leader, roleLeaderMap, seenBy, unreachable)
  }

  def receive: Receive = {
    case MemberUp(member) ⇒
      log.info(s"Member is Up: ${member.address}")

    case UnreachableMember(member) ⇒
      log.info(s"Member detected as unreachable: $member")

    case MemberRemoved(member, previousStatus) ⇒
      log.info(s"Member is Removed: ${member.address} after $previousStatus")

    case LeaderChanged(leader) =>
      log.info(s"Leader changed $leader")

    case lcs: LogClusterState =>
      val s = clusterState
      if (lcs.force) log.info(s.toString)
      else if (s.unreachable.nonEmpty || s.members.size < 3)
        log.warning(s"not all members in cluster: ${s.toString}")

    case _: MemberEvent ⇒ // ignore

  }
}