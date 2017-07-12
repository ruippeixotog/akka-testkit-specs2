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
    def getMessage: GetMessageFunc[A]

    def apply[S <: TestKitBase](t: Expectable[S]) = result(getMessage(t.value, tf(t.value)).result, t)
  }

  class ReceiveMatcher[A](val getMessage: GetMessageFunc[A])(implicit tf: TimeoutFunc)
      extends BaseReceiveMatcher[A] {

    def unwrap[B](f: A => B) =
      new ReceiveMatcher[B](getMessage.andThen(_.mapTransform(ValueCheck.alwaysOk, f)))

    def unwrapPf[B](f: PartialFunction[A, B]) =
      new ReceiveMatcher[B](getMessage.andThen(_.mapTransform(f.andThen(_ => ok), f)))

    def apply(msg: A) = new CheckedReceiveMatcher(getMessage, msg)
    def which[R: AsResult](f: A => R) = new CheckedReceiveMatcher(getMessage, f)
    def like[R: AsResult](f: PartialFunction[A, R]) = new CheckedReceiveMatcher(getMessage, f)
    def allOf(msgs: A*) = new AllOfReceiveMatcher(getMessage, msgs)
    def afterOthers = new AfterOthersReceiveMatcher(getMessage)
  }

  class UntypedReceiveMatcher(_getMessage: GetMessageFunc[AnyRef])(implicit tf: TimeoutFunc)
      extends ReceiveMatcher[Any](_getMessage) {

    def apply[A: ClassTag] =
      new ReceiveMatcher[A](_getMessage.andThen(_.mapTransform[A](beAnInstanceOf[A], _.asInstanceOf[A])))
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

    val getMessage = _getMessage.andThen(_.mapCheck(check))

    def afterOthers = new AfterOthersReceiveMatcher(getMessage)
  }

  class AfterOthersReceiveMatcher[A](_getMessage: GetMessageFunc[A])(implicit tf: TimeoutFunc)
      extends BaseReceiveMatcher[A] {

    val getMessage = { (probe: TestKitBase, timeout: FiniteDuration) =>
      def now = System.nanoTime.nanos
      val stop = now + timeout

      def recv: ResultValue[A] = {
        _getMessage(probe, stop - now) match {
          case r @ SuccessValue(_, _) => r
          case FailureValue(_, CheckFailed) => recv
          case FailureValue(_, ReceiveTimeout) =>
            val koMessage = s"Timeout ($timeout) while waiting for matching message"
            FailureValue.timeout(koMessage)
        }
      }
      recv
    }
  }

  class BaseAllOfReceiveMatcher[A](_getMessage: GetMessageFunc[A], msgs: Seq[A])(implicit tf: TimeoutFunc)
      extends BaseReceiveMatcher[Seq[A]] {

    protected def getRemainingMessages(remMsgs: Seq[A]): GetMessageFunc[A] =
      _getMessage.andThen(_.mapCheck(beOneOf(remMsgs: _*)))

    val getMessage = { (probe: TestKitBase, timeout: FiniteDuration) =>
      def now = System.nanoTime.nanos
      val stop = now + timeout

      def recv(missing: Seq[A], received: Seq[A]): ResultValue[Seq[A]] = {
        if (missing.isEmpty) SuccessValue(Success("Received all messages"), msgs)
        else {
          getRemainingMessages(missing)(probe, stop - now) match {
            case SuccessValue(_, msg) => recv(missing.diff(msg :: Nil), received :+ msg)

            case r @ FailureValue(res, CheckFailed) =>
              received match {
                case Nil => r
                case recvMsg +: Nil => FailureValue.failedCheck(
                  s"Received message '$recvMsg' and ${res.message.uncapitalize}")
                case recvMsgs => FailureValue.failedCheck(
                  s"Received messages '${recvMsgs.mkString(", ")}' and ${res.message.uncapitalize}")
              }

            case FailureValue(_, ReceiveTimeout) =>
              val missingMsgs = missing.mkString(", ")
              val messageWord = if (missingMsgs.length > 1) "messages" else "message"
              FailureValue.timeout(s"Timeout ($timeout) while waiting for $messageWord '$missingMsgs'")
          }
        }
      }
      recv(msgs, Vector.empty)
    }
  }

  class AllOfReceiveMatcher[A](_getMessage: GetMessageFunc[A], msgs: Seq[A])(implicit tf: TimeoutFunc)
      extends BaseAllOfReceiveMatcher[A](_getMessage, msgs) {

    def afterOthers = new AllOfAfterOthersReceiveMatcher(_getMessage, msgs)
  }

  class AllOfAfterOthersReceiveMatcher[A](_getMessage: GetMessageFunc[A], msgs: Seq[A])(implicit tf: TimeoutFunc)
      extends BaseAllOfReceiveMatcher[A](_getMessage, msgs) {

    override protected def getRemainingMessages(remMsgs: Seq[A]): GetMessageFunc[A] =
      new AfterOthersReceiveMatcher(super.getRemainingMessages(remMsgs)).getMessage
  }
}
