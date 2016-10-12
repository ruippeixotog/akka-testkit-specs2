package net.ruippeixotog.akka.testkit.specs2.mutable

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestKit

import AkkaSpecification._

class AkkaSpecification(_system: ActorSystem = actorSystemForClass(getClass))
  extends TestKit(_system) with AkkaSpecificationLike

object AkkaSpecification {
  private def sanitizeName(name: String) = name.replaceAll("[^a-zA-Z0-9-]", "_")
  private def actorSystemForClass(clazz: Class[_]) = ActorSystem(sanitizeName(clazz.getName) + "_" + UUID.randomUUID())
}
