package net.ruippeixotog.akka.testkit.specs2.impl

import org.specs2.execute.AsResult.given
import org.specs2.execute.Result
import org.specs2.matcher.Expectable
import org.specs2.matcher.ExpectedResults.ok
import org.specs2.matcher.ValueCheck
import org.specs2.matcher.ValueChecks.given
import org.specs2.matcher.Matcher

import net.ruippeixotog.akka.testkit.specs2.Util._
import net.ruippeixotog.akka.testkit.specs2.api.ReceiveMatcher
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.TimeoutFunc
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.GetMessageFunc

object CompatMatchers {
  type MatcherResult[_] = Result

  val okResult = ok

  def toMatcherResult[T](r: Result, value: Expectable[T]): MatcherResult[T] = r
  def toValueCheck[T](matcher: Matcher[T]): ValueCheck[T] = matcher.check
}
