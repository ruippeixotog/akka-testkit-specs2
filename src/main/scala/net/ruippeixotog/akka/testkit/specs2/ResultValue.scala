package net.ruippeixotog.akka.testkit.specs2

import org.specs2.execute.{ Failure, Result, Success }
import org.specs2.matcher.ValueCheck

import net.ruippeixotog.akka.testkit.specs2.ResultValue.FailureReason

sealed trait ResultValue[+A] {
  def result: Result
}

case class SuccessValue[A](result: Result, value: A) extends ResultValue[A]
case class FailureValue(result: Result, reason: FailureReason) extends ResultValue[Nothing]

object ResultValue {
  sealed trait FailureReason
  case object ReceiveTimeout extends FailureReason
  case object CheckFailed extends FailureReason

  implicit class ResultValueOps[A](val res: ResultValue[A]) extends AnyVal {

    def mapCheck(check: ValueCheck[A]): ResultValue[A] = res match {
      case fail: FailureValue => fail
      case SuccessValue(result, value) =>
        val newResult = check.check(value)
        if (newResult.isSuccess) SuccessValue(Success(s"${result.message} and ${newResult.message}"), value)
        else FailureValue(Failure(s"${result.message} but ${newResult.message}"), CheckFailed)
    }

    def mapTransform[B](check: ValueCheck[A], g: A => B): ResultValue[B] = res match {
      case fail: FailureValue => fail
      case SuccessValue(result, value) =>
        val newResult = check.check(value)
        if (newResult.isSuccess) SuccessValue(result, g(value))
        else FailureValue(Failure(s"${result.message} but ${newResult.message}"), CheckFailed)
    }
  }
}
