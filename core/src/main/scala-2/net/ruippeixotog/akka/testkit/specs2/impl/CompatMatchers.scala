package net.ruippeixotog.akka.testkit.specs2.impl

import net.ruippeixotog.akka.testkit.specs2.api.ReceiveMatcher
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.TimeoutFunc
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.GetMessageFunc
import org.specs2.matcher.Expectable
import org.specs2.matcher.MatchResult
import org.specs2.matcher.StandardMatchResults.ok
import net.ruippeixotog.akka.testkit.specs2.Util._
import org.specs2.matcher.ValueCheck
import org.specs2.matcher.Matchers._

private[specs2] object CompatMatchers {
  abstract class ReceiveMatcherImpl[P, A](implicit tf: TimeoutFunc[P]) extends ReceiveMatcher[P, A] {
    def getMessage: GetMessageFunc[P, A]

    def apply[S <: P](t: Expectable[S]): MatchResult[S] =
      result(getMessage(t.value, tf(t.value)).result, t)

  }

  def getRemainingMessages[P, A](_getMessage: GetMessageFunc[P, A], remMsgs: Seq[A]): GetMessageFunc[P, A] =
    _getMessage.andThen(_.mapCheck(beOneOf(remMsgs *)))

  def partialToOk[A, B](f: PartialFunction[A, B]): ValueCheck[A] = f.andThen(_ => ok)

  // def getRemainingMessages[P, A](_getMessage: GetMessageFunc[P, A], remMsgs: Seq[A]): GetMessageFunc[P, A] =
  //   _getMessage.andThen(_.mapCheck(beOneOf(remMsgs *)))
}
