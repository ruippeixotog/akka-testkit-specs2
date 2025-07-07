package net.ruippeixotog.pekko.testkit.specs2.mutable

import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.AfterSpec
import org.specs2.specification.core.Fragments

import net.ruippeixotog.pekko.testkit.specs2.PekkoTypedMatchers

trait PekkoTypedSpecificationLike extends SpecificationLike with PekkoTypedMatchers with AfterSpec {
  def testKit: ActorTestKit
  def afterSpec: Fragments = step(testKit.shutdownTestKit())
}
