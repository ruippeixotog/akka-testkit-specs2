package net.ruippeixotog.akka.testkit.specs2

import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.reflect._

import akka.testkit.TestKitBase
import org.specs2.execute.{ AsResult, Failure, Success }
import org.specs2.matcher.{ Expectable, MatchResult, Matcher, ValueCheck }
import org.specs2.specification.SpecificationFeatures

import net.ruippeixotog.akka.testkit.specs2.ResultValue.{ CheckFailed, ReceiveTimeout }
import net.ruippeixotog.akka.testkit.specs2.Util._

trait AkkaMatchers { this: SpecificationFeatures =>

  type TimeoutFunc = TestKitBase => FiniteDuration
  type GetMessageFunc[+A] = (TestKitBase, FiniteDuration) => ResultValue[A]

  def receive = UntypedReceiveMatcher(
    { msg => s"Received message '$msg'" },
    { timeout => s"Timeout ($timeout) while waiting for message" },
    _.remainingOrDefault)

  def receiveWithin(max: FiniteDuration) = UntypedReceiveMatcher(
    { msg => s"Received message '$msg' within $max" },
    { timeout => s"Didn't receive any message within $timeout" },
    { _ => max })

  def receiveMessage = receive
  def receiveMessageWithin(max: FiniteDuration) = receiveWithin(max)

  abstract class BaseReceiveMatcher[A](implicit tf: TimeoutFunc) extends Matcher[TestKitBase] {
    protected def getMessage: GetMessageFunc[A]

    def apply[S <: TestKitBase](t: Expectable[S]) = result(getMessage(t.value, tf(t.value)).result, t)
  }

  class ReceiveMatcher[A](protected val getMessage: GetMessageFunc[A])(implicit tf: TimeoutFunc)
      extends BaseReceiveMatcher[A] {

    def unwrap[B](f: A => B) =
      new ReceiveMatcher[B](getMessage.andThen(_.mapTransform(ValueCheck.alwaysOk, f)))

    def apply(msg: A) = new CheckedReceiveMatcher(getMessage, msg)
    def which[R: AsResult](f: A => R) = new CheckedReceiveMatcher(getMessage, f)
    def like[R: AsResult](f: PartialFunction[A, R]) = new CheckedReceiveMatcher(getMessage, f)
    def allOf[R: AsResult](msgs: A*) = new AllOfReceiveMatcher(getMessage, msgs)
  }

  class UntypedReceiveMatcher(getMessage: GetMessageFunc[AnyRef])(implicit tf: TimeoutFunc)
      extends ReceiveMatcher[Any](getMessage) {

    def apply[A: ClassTag] =
      new ReceiveMatcher[A](getMessage.andThen(_.mapTransform[A](beAnInstanceOf[A], _.asInstanceOf[A])))
  }

  object UntypedReceiveMatcher {
    def apply(receiveOkMsg: AnyRef => String, receiveKoMsg: FiniteDuration => String, timeoutFunc: TimeoutFunc) = {
      val getMessage = { (probe: TestKitBase, timeout: FiniteDuration) =>
        probe.receiveOne(timeout) match {
          case null => FailureValue(Failure(receiveKoMsg(timeout)), ReceiveTimeout)
          case msg => SuccessValue(Success(receiveOkMsg(msg)), msg)
        }
      }
      new UntypedReceiveMatcher(getMessage)(timeoutFunc)
    }
  }

  class CheckedReceiveMatcher[A](_getMessage: GetMessageFunc[A], check: ValueCheck[A])(implicit tf: TimeoutFunc)
      extends BaseReceiveMatcher[A] {

    protected val getMessage = _getMessage.andThen(_.mapCheck(check))

    def afterOthers = new AfterOthersReceiveMatcher(getMessage)
  }

  class AfterOthersReceiveMatcher[A](getMessage: GetMessageFunc[A])(implicit tf: TimeoutFunc)
      extends Matcher[TestKitBase] {

    def apply[S <: TestKitBase](t: Expectable[S]) = {
      def now = System.nanoTime.nanos
      val stop = now + tf(t.value)

      def recv: MatchResult[S] = {
        getMessage(t.value, stop - now) match {
          case SuccessValue(res, _) => result(res, t)
          case FailureValue(_, CheckFailed) => recv
          case FailureValue(_, ReceiveTimeout) =>
            result(false, "", s"Timeout (${tf(t.value)}) while waiting for matching message", t)
        }
      }
      recv
    }
  }

  class AllOfReceiveMatcher[A](getMessage: GetMessageFunc[A], msgs: Seq[A])(implicit tf: TimeoutFunc)
      extends Matcher[TestKitBase] {

    def apply[S <: TestKitBase](t: Expectable[S]) = {
      def now = System.nanoTime.nanos
      val stop = now + tf(t.value)

      def recv(missing: Seq[A]): MatchResult[S] = {
        if (missing.isEmpty) result(true, "Received all messages", "", t)
        else {
          getMessage(t.value, stop - now) match {
            case SuccessValue(_, msg) if missing.contains(msg) => recv(missing.diff(msg :: Nil))
            case SuccessValue(_, msg) => result(false, "", s"Received unexpected message '$msg'", t)
            case FailureValue(res, CheckFailed) => result(res, t)
            case FailureValue(_, ReceiveTimeout) =>
              val missingMsgs = missing.map { msg => s"'$msg'" }.mkString(", ")
              val koMessage = s"Timeout (${tf(t.value)}) while waiting for messages $missingMsgs"
              result(false, "", koMessage, t)
          }
        }
      }
      recv(msgs)
    }
  }
}
