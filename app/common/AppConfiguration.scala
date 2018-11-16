package common

import akka.util.Timeout
import javax.inject.{Inject, Singleton}
import play.api.Configuration

import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps


@Singleton
class AppConfiguration @Inject()(config: Configuration) {

  val timeout: Timeout = config.get[FiniteDuration]("application.timeout")

  // Persistence
  val saveSnapshotInterval: Int = config.get[Int]("application.saveSnapshotInterval")

}