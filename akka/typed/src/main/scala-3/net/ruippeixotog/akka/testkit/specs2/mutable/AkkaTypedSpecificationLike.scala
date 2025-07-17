package net.ruippeixotog.akka.testkit.specs2.mutable

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.AfterSpec
import org.specs2.specification.core.Fragments

import net.ruippeixotog.akka.testkit.specs2.AkkaTypedMatchers

trait AkkaTypedSpecificationLike extends SpecificationLike with AkkaTypedMatchers with AfterSpec {
  def testKit: ActorTestKit
  def afterSpec: Fragments = step(testKit.shutdownTestKit())
}
