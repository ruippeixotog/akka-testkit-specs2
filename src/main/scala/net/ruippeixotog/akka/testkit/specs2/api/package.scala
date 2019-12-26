package net.ruippeixotog.akka.testkit.specs2

import scala.reflect.ClassTag

import org.specs2.execute.AsResult
import org.specs2.matcher.Matcher

package object api {
  trait BaseReceiveMatcher[P, A] extends Matcher[P]

  trait SkippableReceiveMatcher[P, A] extends BaseReceiveMatcher[P, A] {
    def afterOthers: BaseReceiveMatcher[P, A]
  }

  trait ReceiveMatcher[P, A] extends SkippableReceiveMatcher[P, A] {
    def unwrap[B](f: A => B): ReceiveMatcher[P, B]
    def unwrapPf[B](f: PartialFunction[A, B]): ReceiveMatcher[P, B]
    def ofSubtype[B <: A: ClassTag](implicit ev: A <:< AnyRef): ReceiveMatcher[P, B]

    def apply(msg: A): SkippableReceiveMatcher[P, A]
    def which[R: AsResult](f: A => R): SkippableReceiveMatcher[P, A]
    def like[R: AsResult](f: PartialFunction[A, R]): SkippableReceiveMatcher[P, A]
    def allOf(msgs: A*): SkippableReceiveMatcher[P, Seq[A]]
  }

  trait UntypedReceiveMatcher[P] extends ReceiveMatcher[P, Any] {
    def apply[A: ClassTag]: ReceiveMatcher[P, A]
  }
}
