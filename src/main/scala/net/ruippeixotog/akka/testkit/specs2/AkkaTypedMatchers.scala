package net.ruippeixotog.akka.testkit.specs2

import scala.concurrent.duration.FiniteDuration

import akka.actor.testkit.typed.scaladsl.TestProbe
import org.specs2.execute.{ Failure, Success }

import net.ruippeixotog.akka.testkit.specs2.ResultValue.ReceiveTimeout
import net.ruippeixotog.akka.testkit.specs2.api.FullReceiveMatcher
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.FullReceiveMatcherImpl

trait AkkaTypedMatchers {

  /**
   * A `Matcher` expecting a probe to have received a message within the default timeout.
   * Additional methods can be chained to constrain the expected message.
   */
  def receive[Msg]: FullReceiveMatcher[TestProbe[Msg], Msg] = {
    akkaTypedReceiveMatcher[Msg](
      { msg => s"Received message '$msg'" },
      { timeout => s"Timeout ($timeout) while waiting for message" },
      _.remainingOrDefault)
  }

  /**
   * A `Matcher` expecting a probe to have received a message within the provided timeout.
   * Additional methods can be chained to constrain the expected message.
   *
   * @param max the timeout for a message to be received
   */
  def receiveWithin[Msg](max: FiniteDuration): FullReceiveMatcher[TestProbe[Msg], Msg] = {
    akkaTypedReceiveMatcher[Msg](
      { msg => s"Received message '$msg' within $max" },
      { timeout => s"Didn't receive any message within $timeout" },
      { _ => max })
  }

  /**
   * An alias for [[receive]].
   */
  def receiveMessage[Msg]: FullReceiveMatcher[TestProbe[Msg], Msg] = receive

  /**
   * An alias for [[receiveWithin]].
   */
  def receiveMessageWithin[Msg](max: FiniteDuration): FullReceiveMatcher[TestProbe[Msg], Msg] = receiveWithin(max)

  private[this] def akkaTypedReceiveMatcher[Msg](
    receiveOkMsg: Msg => String,
    receiveKoMsg: FiniteDuration => String,
    timeoutFunc: TestProbe[Msg] => FiniteDuration): FullReceiveMatcher[TestProbe[Msg], Msg] = {

    val getMessage = { (probe: TestProbe[Msg], timeout: FiniteDuration) =>
      try {
        val msg = probe.receiveMessage(timeout)
        SuccessValue(Success(receiveOkMsg(msg)), msg)
      } catch {
        case _: AssertionError => FailureValue(Failure(receiveKoMsg(timeout)), ReceiveTimeout)
      }
    }
    new FullReceiveMatcherImpl[TestProbe[Msg], Msg](getMessage)(timeoutFunc)
  }
}
