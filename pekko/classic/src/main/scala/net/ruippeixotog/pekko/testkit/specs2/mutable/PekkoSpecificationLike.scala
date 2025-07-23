package net.ruippeixotog.pekko.testkit.specs2.mutable

import org.apache.pekko.testkit.TestKitBase
import org.specs2.mutable.SpecificationLike
import net.ruippeixotog.akka.testkit.specs2.AfterAllCompat

import net.ruippeixotog.pekko.testkit.specs2.PekkoMatchers

trait PekkoSpecificationLike extends TestKitBase with SpecificationLike with PekkoMatchers with AfterAllCompat {
  def shutdownTestkit(): Unit = shutdown()
}
