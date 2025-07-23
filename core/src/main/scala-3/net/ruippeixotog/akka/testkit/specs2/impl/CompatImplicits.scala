package net.ruippeixotog.akka.testkit.specs2.impl

import org.specs2.execute.AsResult
import org.specs2.execute.Result
import org.specs2.matcher.{Expectable, Matcher, ValueCheck, ValueChecks}

object CompatImplicits {
  // These pulls the needed implicits so that they can be imported with `._` in Scala 3 instead of `.given`
  implicit def partialfunctionIsValueCheck[T, R: AsResult]: Conversion[PartialFunction[T, R], ValueCheck[T]] =
    ValueChecks.partialfunctionIsValueCheck[T, R]
  implicit def functionIsValueCheck[T, R: AsResult]: Conversion[T => R, ValueCheck[T]] =
    ValueChecks.functionIsValueCheck[T, R]
  implicit def matcherIsValueCheck[T]: Conversion[Matcher[T], ValueCheck[T]] = ValueChecks.matcherIsValueCheck[T]

  implicit def asResult[R](using convert: R => Result): AsResult[R] = AsResult.asResult[R]
}
