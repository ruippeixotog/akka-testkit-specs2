package net.ruippeixotog.akka.testkit.specs2.impl

import org.specs2.matcher.Expectable
import org.specs2.matcher.Matchers._
import org.specs2.matcher.MatchResult
import org.specs2.matcher.StandardMatchResults.ok
import org.specs2.matcher.ValueCheck

import net.ruippeixotog.akka.testkit.specs2.Util._
import net.ruippeixotog.akka.testkit.specs2.api.ReceiveMatcher
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.TimeoutFunc
import net.ruippeixotog.akka.testkit.specs2.impl.Matchers.GetMessageFunc
import org.specs2.execute.Result
import org.specs2.matcher.Matcher

private[specs2] object CompatMatchers {
  type MatcherResult[S] = MatchResult[S]
  val okResult = ok

  def toMatcherResult[T](r: Result, value: Expectable[T]): MatcherResult[T] = Matcher.result(r, value)
  def toValueCheck[T](matcher: Matcher[T]): ValueCheck[T] = matcher
}
