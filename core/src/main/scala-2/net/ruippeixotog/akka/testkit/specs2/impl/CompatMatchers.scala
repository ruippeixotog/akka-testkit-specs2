package net.ruippeixotog.akka.testkit.specs2.impl

import org.specs2.matcher.Expectable
import org.specs2.matcher.Matchers._
import org.specs2.matcher.MatchResult
import org.specs2.matcher.StandardMatchResults.ok
import org.specs2.matcher.ValueCheck
import org.specs2.matcher.Matcher.result

import net.ruippeixotog.akka.testkit.specs2.Util._
import net.ruippeixotog.akka.testkit.specs2.api.ReceiveMatcher
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.TimeoutFunc
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.GetMessageFunc

private[specs2] object CompatMatchers {
  type MatcherResult[S] = MatchResult[S]

  def createMatcher[P, A, S <: P, R](getMessage: GetMessageFunc[P, A], t: Expectable[S])(implicit
      tf: TimeoutFunc[P]
  ): MatchResult[S] =
    result(getMessage(t.value, tf(t.value)).result, t)

  def getRemainingMessages[P, A](_getMessage: GetMessageFunc[P, A], remMsgs: Seq[A]): GetMessageFunc[P, A] =
    _getMessage.andThen(_.mapCheck(beOneOf(remMsgs *)))

  def partialToOk[A, B](f: PartialFunction[A, B]): ValueCheck[A] = f.andThen(_ => ok)
}
