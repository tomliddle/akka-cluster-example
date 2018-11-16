package models.persist

import java.time.ZonedDateTime

import akka.actor.Props
import akka.persistence.{PersistentActor, SaveSnapshotSuccess}
import akka.util.Timeout
import common.AppConfiguration
import models.persist.PersistActor.TestMessage
import play.api.Application
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

object PersistActor {

  val Name = "persistentactor"

  def props(
     ws: WSClient,
     app: Application,
     config: AppConfiguration,
    ec: ExecutionContext): Props = {
    Props(new PersistActor(ws, app, config, ec))
  }

  case class MessageConfirmation(msg: String)

  case class TestMessage(id: String, time: ZonedDateTime)


  case class State(messages: Map[String, TestMessage] = Map[String, TestMessage]()) {

    def add(r: TestMessage): State = State(messages + (r.id -> r))

    def delete(id: String): State = State(messages - id)

    def deleteSeq(id: Iterable[String]): State = State(messages -- id)
  }

  sealed trait Evt
  case class Save(r: TestMessage) extends Evt
  case class Delete(communicationId: String) extends Evt
  case class DeleteSeq(communicationIds: Iterable[String]) extends Evt

}

/**
  * Handles requesting streams and recording of what streams are running
  * TODO - should we add AtLeastOnceDelivery?
  */
class PersistActor(
                    val ws: WSClient,
                    val app: Application,
                    val config: AppConfiguration,
                    implicit val ec: ExecutionContext
                    ) extends PersistentActor with ActorPersistBehaviour {

  implicit val timeout: Timeout = config.timeout
  override val saveSnapshotInterval: Int = config.saveSnapshotInterval

  override def preStart(): Unit = log.info("prestart")
  override def preRestart(reason: Throwable, msg: Option[Any]): Unit = log.info(s"prerestart reason:${reason.getMessage} msg:$msg")
  override def postStop(): Unit = log.info("stopping")

  override val receiveCommand: Receive = {

    case TestMessage =>
      log.info("received test message")

    case s: SaveSnapshotSuccess =>
      log.info(s"Snapshot success")

    case x =>
      log.warning(s"unhandled message $x")

  }
}