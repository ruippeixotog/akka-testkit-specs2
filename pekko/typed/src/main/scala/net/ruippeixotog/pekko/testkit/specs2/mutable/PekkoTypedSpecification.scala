package net.ruippeixotog.pekko.testkit.specs2.mutable

import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.specs2.mutable.Specification

abstract class PekkoTypedSpecification(val testKit: ActorTestKit = ActorTestKit())
    extends Specification
    with PekkoTypedSpecificationLike
