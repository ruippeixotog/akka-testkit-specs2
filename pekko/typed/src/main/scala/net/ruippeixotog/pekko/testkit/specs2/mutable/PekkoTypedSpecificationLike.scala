package net.ruippeixotog.pekko.testkit.specs2.mutable

import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.specs2.mutable.SpecificationLike

import net.ruippeixotog.akka.testkit.specs2.AfterAllCompat
import net.ruippeixotog.pekko.testkit.specs2.PekkoTypedMatchers

trait PekkoTypedSpecificationLike extends SpecificationLike with PekkoTypedMatchers with AfterAllCompat {
  def testKit: ActorTestKit
  def shutdownTestkit(): Unit = testKit.shutdownTestKit()
}
