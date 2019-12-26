package net.ruippeixotog.akka.testkit.specs2.mutable

import akka.actor.testkit.typed.scaladsl.ActorTestKit

abstract class AkkaTypedSpecification(val testKit: ActorTestKit = ActorTestKit())
  extends AkkaTypedSpecificationLike
