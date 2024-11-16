package net.ruippeixotog.pekko.testkit.specs2

import scala.concurrent.duration.FiniteDuration

import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe

import net.ruippeixotog.akka.testkit.specs2.impl.TypedMatchers

trait PekkoTypedMatchers extends TypedMatchers[TestProbe] {
  protected def defaultReceiveTimeout[Msg](testkit: TestProbe[Msg]): FiniteDuration =
    testkit.remainingOrDefault

  protected def receiveOne[Msg](probe: TestProbe[Msg], timeout: FiniteDuration): Msg =
    probe.receiveMessage(timeout)
}
