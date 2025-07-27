package net.ruippeixotog.akka.testkit.specs2.impl

import org.specs2.execute.AsResult.given
import org.specs2.execute.Result
import org.specs2.matcher.Expectable
import org.specs2.matcher.ExpectedResults.ok
import org.specs2.matcher.Matchers._
import org.specs2.matcher.ValueCheck
import org.specs2.matcher.ValueChecks.given

import net.ruippeixotog.akka.testkit.specs2.Util._
import net.ruippeixotog.akka.testkit.specs2.api.ReceiveMatcher
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.TimeoutFunc
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.GetMessageFunc

object CompatMatchers {
  type MatcherResult[_] = Result

  def createMatcher[P, A, S <: P, R](getMessage: GetMessageFunc[P, A], t: Expectable[S])(implicit
      tf: TimeoutFunc[P]
  ): MatcherResult[S] = getMessage(t.value, tf(t.value)).result

  def getRemainingMessages[P, A](_getMessage: GetMessageFunc[P, A], remMsgs: Seq[A]): GetMessageFunc[P, A] =
    _getMessage.andThen(_.mapCheck(beOneOf[A](remMsgs *).check))

  def partialToOk[A, B](f: PartialFunction[A, B]): ValueCheck[A] = f.andThen(_ => ok)
}
