package net.ruippeixotog.akka.testkit.specs2.interop

import net.ruippeixotog.akka.testkit.specs2.ResultValue.ReceiveTimeout
import net.ruippeixotog.akka.testkit.specs2.api._
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers._
import net.ruippeixotog.akka.testkit.specs2.{FailureValue, SuccessValue}
import org.specs2.execute.{Failure, Success}
import scala.concurrent.duration.FiniteDuration

trait ClassicMatchers[TestKitBase] {
  protected def defaultReceiveTimeout(testkit: TestKitBase): FiniteDuration
  protected def receiveOne(probe: TestKitBase, timeout: FiniteDuration): AnyRef

  private[this] def classicReceiveMatcher(
      receiveOkMsg: AnyRef => String,
      receiveKoMsg: FiniteDuration => String,
      timeoutFunc: TestKitBase => FiniteDuration
  ): UntypedFullReceiveMatcher[TestKitBase] = {

    val getMessage = { (probe: TestKitBase, timeout: FiniteDuration) =>
      receiveOne(probe, timeout) match {
        case null => FailureValue(Failure(receiveKoMsg(timeout)), ReceiveTimeout)
        case msg => SuccessValue(Success(receiveOkMsg(msg)), msg)
      }
    }
    new UntypedFullReceiveMatcherImpl[TestKitBase](getMessage)(timeoutFunc)
  }

  /** A `Matcher` expecting a probe to have received a message within the default timeout. Additional methods can be
    * chained to constrain the expected message.
    */
  def receive: UntypedFullReceiveMatcher[TestKitBase] = {
    classicReceiveMatcher(
      { msg => s"Received message '$msg'" },
      { timeout => s"Timeout ($timeout) while waiting for message" },
      defaultReceiveTimeout
    )
  }

  /** A `Matcher` expecting a probe to have received a message within the provided timeout. Additional methods can be
    * chained to constrain the expected message.
    *
    * @param max
    *   the timeout for a message to be received
    */
  def receiveWithin(max: FiniteDuration): UntypedFullReceiveMatcher[TestKitBase] = {
    classicReceiveMatcher(
      { msg => s"Received message '$msg' within $max" },
      { timeout => s"Didn't receive any message within $timeout" },
      { _ => max }
    )
  }

  /** An alias for [[receive]].
    */
  def receiveMessage: UntypedFullReceiveMatcher[TestKitBase] = receive

  /** An alias for [[receiveWithin]].
    */
  def receiveMessageWithin(max: FiniteDuration): UntypedFullReceiveMatcher[TestKitBase] = receiveWithin(max)
}
