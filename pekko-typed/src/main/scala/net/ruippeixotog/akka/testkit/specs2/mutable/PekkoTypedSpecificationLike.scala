package net.ruippeixotog.akka.testkit.specs2.mutable

import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.AfterAll

import net.ruippeixotog.akka.testkit.specs2.PekkoTypedMatchers

trait PekkoTypedSpecificationLike extends SpecificationLike with PekkoTypedMatchers with AfterAll {
  def testKit: ActorTestKit
  def afterAll(): Unit = testKit.shutdownTestKit()
}
