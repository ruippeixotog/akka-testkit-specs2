package net.ruippeixotog.pekko.testkit.specs2.mutable

import org.apache.pekko.testkit.TestKitBase
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.AfterAll

import net.ruippeixotog.pekko.testkit.specs2.PekkoMatchers

trait PekkoSpecificationLike extends TestKitBase with SpecificationLike with PekkoMatchers with AfterAll {
  def afterAll(): Unit = shutdown()
}
