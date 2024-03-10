package net.ruippeixotog.akka.testkit.specs2.mutable

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.AfterAll

import net.ruippeixotog.akka.testkit.specs2.AkkaTypedMatchers

trait AkkaTypedSpecificationLike extends SpecificationLike with AkkaTypedMatchers with AfterAll {
  def testKit: ActorTestKit
  def afterAll(): Unit = testKit.shutdownTestKit()
}
