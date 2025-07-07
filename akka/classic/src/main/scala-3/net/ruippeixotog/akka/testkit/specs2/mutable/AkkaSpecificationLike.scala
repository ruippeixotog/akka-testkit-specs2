package net.ruippeixotog.akka.testkit.specs2.mutable

import akka.testkit.TestKitBase
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.AfterSpec
import org.specs2.specification.core.Fragments

import net.ruippeixotog.akka.testkit.specs2.AkkaMatchers

trait AkkaSpecificationLike extends TestKitBase with SpecificationLike with AkkaMatchers with AfterSpec {
  def afterSpec: Fragments = step(shutdown())
}
