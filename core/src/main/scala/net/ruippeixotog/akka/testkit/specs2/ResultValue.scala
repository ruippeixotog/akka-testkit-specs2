package net.ruippeixotog.akka.testkit.specs2

import org.specs2.execute.{Failure, Result, Success}
import org.specs2.matcher.ValueCheck

import net.ruippeixotog.akka.testkit.specs2.ResultValue.{CheckFailed, FailureReason, ReceiveTimeout}

sealed trait ResultValue[+A] {
  def result: Result
}

case class SuccessValue[A](result: Result, value: A) extends ResultValue[A]

object SuccessValue {
  def apply[A](msg: String, value: A): SuccessValue[A] = SuccessValue(Success(msg), value)
}

case class FailureValue(result: Result, reason: FailureReason) extends ResultValue[Nothing]

object FailureValue {
  def apply(msg: String, reason: FailureReason): FailureValue = FailureValue(Failure(msg), reason)
  def timeout(msg: String): FailureValue = FailureValue(msg, ReceiveTimeout)
  def failedCheck(msg: String): FailureValue = FailureValue(msg, CheckFailed)
}

object ResultValue {
  sealed trait FailureReason
  case object ReceiveTimeout extends FailureReason
  case object CheckFailed extends FailureReason

  implicit class ResultValueOps[A](val res: ResultValue[A]) extends AnyVal {

    def mapCheck(check: ValueCheck[A]): ResultValue[A] = res match {
      case fail: FailureValue => fail
      case SuccessValue(result, value) =>
        val newResult = check.check(value)
        if (newResult.isSuccess) SuccessValue(s"${result.message} and ${newResult.message}", value)
        else FailureValue.failedCheck(s"${result.message} but ${newResult.message}")
    }

    def mapTransform[B](check: ValueCheck[A], g: A => B): ResultValue[B] = res match {
      case fail: FailureValue => fail
      case SuccessValue(result, value) =>
        val newResult = check.check(value)
        if (newResult.isSuccess) SuccessValue(result, g(value))
        else FailureValue.failedCheck(s"${result.message} but ${newResult.message}")
    }
  }
}
