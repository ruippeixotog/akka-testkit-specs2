package net.ruippeixotog.pekko.testkit.specs2.mutable

import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit

abstract class PekkoTypedSpecification(val testKit: ActorTestKit = ActorTestKit()) extends PekkoTypedSpecificationLike
