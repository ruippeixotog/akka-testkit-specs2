package net.ruippeixotog.akka.testkit.specs2.mutable

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.specs2.mutable.SpecificationLike

import net.ruippeixotog.akka.testkit.specs2.AfterAllCompat
import net.ruippeixotog.akka.testkit.specs2.AkkaTypedMatchers

trait AkkaTypedSpecificationLike extends SpecificationLike with AkkaTypedMatchers with AfterAllCompat {
  def testKit: ActorTestKit
  def shutdownTestkit(): Unit = testKit.shutdownTestKit()
}
