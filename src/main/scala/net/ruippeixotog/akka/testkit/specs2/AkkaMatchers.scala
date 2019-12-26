package net.ruippeixotog.akka.testkit.specs2

import scala.concurrent.duration.FiniteDuration

import akka.testkit.TestKitBase
import org.specs2.execute.{ Failure, Success }
import org.specs2.specification.SpecificationFeatures

import net.ruippeixotog.akka.testkit.specs2.ResultValue.ReceiveTimeout
import net.ruippeixotog.akka.testkit.specs2.api.UntypedReceiveMatcher
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers._

trait AkkaMatchers { this: SpecificationFeatures =>

  def receive: UntypedReceiveMatcher[TestKitBase] = {
    akkaClassicReceiveMatcher(
      { msg => s"Received message '$msg'" },
      { timeout => s"Timeout ($timeout) while waiting for message" },
      _.remainingOrDefault)
  }

  def receiveWithin(max: FiniteDuration): UntypedReceiveMatcher[TestKitBase] = {
    akkaClassicReceiveMatcher(
      { msg => s"Received message '$msg' within $max" },
      { timeout => s"Didn't receive any message within $timeout" },
      { _ => max })
  }

  def receiveMessage: UntypedReceiveMatcher[TestKitBase] = receive
  def receiveMessageWithin(max: FiniteDuration): UntypedReceiveMatcher[TestKitBase] = receiveWithin(max)

  private[this] def akkaClassicReceiveMatcher(
    receiveOkMsg: AnyRef => String,
    receiveKoMsg: FiniteDuration => String,
    timeoutFunc: TestKitBase => FiniteDuration): UntypedReceiveMatcher[TestKitBase] = {

    val getMessage = { (probe: TestKitBase, timeout: FiniteDuration) =>
      probe.receiveOne(timeout) match {
        case null => FailureValue(Failure(receiveKoMsg(timeout)), ReceiveTimeout)
        case msg => SuccessValue(Success(receiveOkMsg(msg)), msg)
      }
    }
    new UntypedReceiveMatcherImpl[TestKitBase](getMessage)(timeoutFunc)
  }
}
