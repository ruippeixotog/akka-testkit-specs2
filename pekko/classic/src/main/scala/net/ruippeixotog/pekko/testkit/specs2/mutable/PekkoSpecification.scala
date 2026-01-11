package net.ruippeixotog.pekko.testkit.specs2.mutable

import java.util.UUID

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.TestKitBase
import org.specs2.mutable.Specification

import PekkoSpecification._

abstract class PekkoSpecification(_system: ActorSystem = actorSystemForClass(getClass))
    extends Specification
    with TestKitBase
    with PekkoSpecificationLike {
  implicit def system: ActorSystem = _system
}

object PekkoSpecification {
  private def sanitizeName(name: String) = name.replaceAll("[^a-zA-Z0-9-]", "_")
  private def actorSystemForClass(clazz: Class[?]) = ActorSystem(sanitizeName(clazz.getName) + "_" + UUID.randomUUID())
}
