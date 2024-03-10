package net.ruippeixotog.akka.testkit.specs2

import scala.concurrent.duration.FiniteDuration

import akka.testkit.TestKitBase

import net.ruippeixotog.akka.testkit.specs2.interop.ClassicMatchers

trait AkkaMatchers extends ClassicMatchers[TestKitBase] {
  protected def defaultReceiveTimeout(testkit: TestKitBase): FiniteDuration =
    testkit.remainingOrDefault

  protected def receiveOne(probe: TestKitBase, timeout: FiniteDuration): AnyRef =
    probe.receiveOne(timeout)
}
