package modules

import com.google.inject.AbstractModule
import models.cluster.ClusterListener
import models.persist.ClusterSingletonSupervisor
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.
  * Note we need to disable this in tests to override with different actors. It appears not possible to disable the default module so
  * we need to give this a different name.
  */
class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {

    java.lang.System.setProperty("user.timezone", "UTC")

    bindActor[ClusterSingletonSupervisor](ClusterSingletonSupervisor.Name)

    bindActor[ClusterListener](ClusterListener.Name)
  }
}
