package net.ruippeixotog.pekko.testkit.specs2.mutable

import java.util.UUID

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.TestKit

import PekkoSpecification._

abstract class PekkoSpecification(_system: ActorSystem = actorSystemForClass(getClass))
    extends TestKit(_system)
    with PekkoSpecificationLike

object PekkoSpecification {
  private def sanitizeName(name: String) = name.replaceAll("[^a-zA-Z0-9-]", "_")
  private def actorSystemForClass(clazz: Class[_]) = ActorSystem(sanitizeName(clazz.getName) + "_" + UUID.randomUUID())
}
