package net.ruippeixotog.akka.testkit.specs2.mutable

import akka.testkit.TestKitBase
import org.specs2.mutable.SpecificationLike

import net.ruippeixotog.akka.testkit.specs2.AfterAllCompat
import net.ruippeixotog.akka.testkit.specs2.AkkaMatchers

trait AkkaSpecificationLike extends TestKitBase with SpecificationLike with AkkaMatchers with AfterAllCompat {
  def shutdownTestkit(): Unit = shutdown()
}
