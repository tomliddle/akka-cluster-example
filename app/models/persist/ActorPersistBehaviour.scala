package models.persist

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotOffer}
import models.persist.PersistActor._

/**
  * Abstract trait to serve as an interface to the persistence functionality
  */
trait Persist {

  // The only state of the app. Should only be updatable via updateState
  protected var state: State = State()

  def persistState(event: Evt): Unit

  def messages: Map[String, TestMessage] = state.messages

  def persistLocalState(event: Evt): Unit = event match {
    case Save(evt) ⇒
      state = state.add(evt)

    case Delete(communicationId) ⇒
      state = state.delete(communicationId)

    case DeleteSeq(communicationIds) =>
      if (communicationIds.nonEmpty)
        state = state.deleteSeq(communicationIds)
  }
}

/**
  */
trait ActorPersistBehaviour extends Persist with ActorLogging {
  this: PersistentActor =>

  override def persistenceId: String = PersistActor.Name

  val saveSnapshotInterval: Int

  override val receiveRecover: Receive = {

    case evt: Evt => persistLocalState(evt)

    case SnapshotOffer(_, snapshot: State) ⇒ state = snapshot
  }

  /**
    * Persist the state. Note we can persist async as we are reading the state from the locally persisted state.
    * All persist (not persistAsync) does is to stash any incoming messages.
    */
  private def persistEvent(event: Evt): Unit = {
    persist(event) { e =>
      log.info(s"event persisted $event")
      if (lastSequenceNr % saveSnapshotInterval == 0 && lastSequenceNr != 0) {
        log.info("saving snapshot")
        saveSnapshot(state)
      }
    }
  }

  override def persistState(event: Evt): Unit = {

    persistLocalState(event)

    // Update persisted state
    event match {
      case DeleteSeq(communicationIds) =>
        if (communicationIds.nonEmpty)
          persistEvent(event)

      case x =>
        persistEvent(event)
    }
  }

  /**
    * The current state, immutable so we can only update the state via an event
    */
  override def messages: Map[String, TestMessage] = state.messages
}
