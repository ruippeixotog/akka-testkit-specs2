package net.ruippeixotog.pekko.testkit.specs2.mutable

import org.apache.pekko.testkit.TestKitBase
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.AfterSpec
import org.specs2.specification.core.Fragments

import net.ruippeixotog.pekko.testkit.specs2.PekkoMatchers

trait PekkoSpecificationLike extends TestKitBase with SpecificationLike with PekkoMatchers with AfterSpec {
  def afterSpec: Fragments = step(shutdown())
}
