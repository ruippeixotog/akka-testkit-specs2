package net.ruippeixotog.akka.testkit.specs2.impl

import scala.annotation.tailrec
import scala.concurrent.duration.{FiniteDuration, _}
import scala.reflect.ClassTag

import org.specs2.execute.{AsResult, Success}
import org.specs2.matcher.Matchers.{beOneOf, _}
import org.specs2.matcher.StandardMatchResults.ok
import org.specs2.matcher.{Expectable, MatchResult, Matcher, ValueCheck}

import net.ruippeixotog.akka.testkit.specs2.ResultValue.{CheckFailed, ReceiveTimeout}
import net.ruippeixotog.akka.testkit.specs2.Util._
import net.ruippeixotog.akka.testkit.specs2.api._
import net.ruippeixotog.akka.testkit.specs2.{FailureValue, ResultValue, SuccessValue}

private[specs2] object Matchers {

  type TimeoutFunc[P] = P => FiniteDuration
  type GetMessageFunc[P, +A] = (P, FiniteDuration) => ResultValue[A]

  abstract class ReceiveMatcherImpl[P, A](implicit tf: TimeoutFunc[P]) extends ReceiveMatcher[P, A] {
    def getMessage: GetMessageFunc[P, A]

    def apply[S <: P](t: Expectable[S]): MatchResult[S] =
      result(getMessage(t.value, tf(t.value)).result, t)
  }

  class FullReceiveMatcherImpl[P, A](val getMessage: GetMessageFunc[P, A])(implicit tf: TimeoutFunc[P])
      extends ReceiveMatcherImpl[P, A]
      with FullReceiveMatcher[P, A] {

    def unwrap[B](f: A => B) =
      new FullReceiveMatcherImpl[P, B](getMessage.andThen(_.mapTransform(ValueCheck.alwaysOk, f)))

    def unwrapPf[B](f: PartialFunction[A, B]) =
      new FullReceiveMatcherImpl[P, B](getMessage.andThen(_.mapTransform(f.andThen(_ => ok), f)))

    def ofSubtype[B <: A: ClassTag](implicit ev: A <:< AnyRef): FullReceiveMatcher[P, B] = {
      // Always true because of `ev` and because `Matcher` is contravariant.
      // In Scala 2.13 we can avoid this with `ev.substituteContra(beAnInstanceOf[B])`.
      val beAnInstanceOfB = beAnInstanceOf[B].asInstanceOf[Matcher[A]]
      new FullReceiveMatcherImpl[P, B](getMessage.andThen(_.mapTransform[B](beAnInstanceOfB, _.asInstanceOf[B])))
    }

    def apply(msg: A) = new CheckedReceiveMatcherImpl(getMessage, msg)
    def which[R: AsResult](f: A => R) = new CheckedReceiveMatcherImpl(getMessage, f)
    def like[R: AsResult](f: PartialFunction[A, R]) = new CheckedReceiveMatcherImpl(getMessage, f)
    def allOf(msgs: A*) = new AllOfReceiveMatcherImpl(getMessage, msgs)
    def afterOthers = new AfterOthersReceiveMatcherImpl(getMessage)
  }

  class UntypedFullReceiveMatcherImpl[P](_getMessage: GetMessageFunc[P, AnyRef])(implicit tf: TimeoutFunc[P])
      extends FullReceiveMatcherImpl[P, Any](_getMessage)
      with UntypedFullReceiveMatcher[P] {

    def apply[A: ClassTag] =
      new FullReceiveMatcherImpl[P, A](_getMessage.andThen(_.mapTransform[A](beAnInstanceOf[A], _.asInstanceOf[A])))
  }

  class CheckedReceiveMatcherImpl[P, A](_getMessage: GetMessageFunc[P, A], check: ValueCheck[A])(implicit
      tf: TimeoutFunc[P]
  ) extends ReceiveMatcherImpl[P, A]
      with SkippableReceiveMatcher[P, A] {

    val getMessage = _getMessage.andThen(_.mapCheck(check))

    def afterOthers = new AfterOthersReceiveMatcherImpl(getMessage)
  }

  class AfterOthersReceiveMatcherImpl[P, A](_getMessage: GetMessageFunc[P, A])(implicit tf: TimeoutFunc[P])
      extends ReceiveMatcherImpl[P, A] {

    val getMessage = { (probe: P, timeout: FiniteDuration) =>
      def now = System.nanoTime.nanos
      val stop = now + timeout

      @tailrec def recv: ResultValue[A] = {
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

  class BaseAllOfReceiveMatcherImpl[P, A](_getMessage: GetMessageFunc[P, A], msgs: Seq[A])(implicit tf: TimeoutFunc[P])
      extends ReceiveMatcherImpl[P, Seq[A]] {

    protected def getRemainingMessages(remMsgs: Seq[A]): GetMessageFunc[P, A] =
      _getMessage.andThen(_.mapCheck(beOneOf(remMsgs: _*)))

    val getMessage = { (probe: P, timeout: FiniteDuration) =>
      def now = System.nanoTime.nanos
      val stop = now + timeout

      @tailrec def recv(missing: Seq[A], received: Seq[A]): ResultValue[Seq[A]] = {
        if (missing.isEmpty) SuccessValue(Success("Received all messages"), msgs)
        else {
          getRemainingMessages(missing)(probe, stop - now) match {
            case SuccessValue(_, msg) => recv(missing.diff(msg :: Nil), received :+ msg)

            case r @ FailureValue(res, CheckFailed) =>
              received match {
                case Nil => r
                case recvMsg +: Nil =>
                  FailureValue.failedCheck(s"Received message '$recvMsg' and ${res.message.uncapitalize}")
                case recvMsgs =>
                  FailureValue.failedCheck(
                    s"Received messages '${recvMsgs.mkString(", ")}' and ${res.message.uncapitalize}"
                  )
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

  class AllOfReceiveMatcherImpl[P, A](_getMessage: GetMessageFunc[P, A], msgs: Seq[A])(implicit tf: TimeoutFunc[P])
      extends BaseAllOfReceiveMatcherImpl[P, A](_getMessage, msgs)
      with SkippableReceiveMatcher[P, Seq[A]] {

    def afterOthers = new AllOfAfterOthersReceiveMatcher(_getMessage, msgs)
  }

  class AllOfAfterOthersReceiveMatcher[P, A](_getMessage: GetMessageFunc[P, A], msgs: Seq[A])(implicit
      tf: TimeoutFunc[P]
  ) extends BaseAllOfReceiveMatcherImpl[P, A](_getMessage, msgs) {

    override protected def getRemainingMessages(remMsgs: Seq[A]): GetMessageFunc[P, A] =
      new AfterOthersReceiveMatcherImpl(super.getRemainingMessages(remMsgs)).getMessage
  }
}
