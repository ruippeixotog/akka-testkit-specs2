package net.ruippeixotog.akka.testkit.specs2

import scala.reflect.ClassTag

import org.specs2.execute.AsResult
import org.specs2.matcher.Matcher

/**
 * The API used to transform and compose matchers for received messages.
 */
package object api {

  trait ReceiveMatcher[P, A] extends Matcher[P]

  trait SkippableReceiveMatcher[P, A] extends ReceiveMatcher[P, A] {

    /**
     * Skips non-matching messages until a matching one is received or a timeout occurs. Commonly used when the order of
     * received messages cannot be guaranteed and the probe may receive other messages, like heartbeats.
     *
     * @return a new matcher that skips non-matching messages until a matching one is received or a timeout occurs.
     */
    def afterOthers: ReceiveMatcher[P, A]
  }

  trait FullReceiveMatcher[P, A] extends SkippableReceiveMatcher[P, A] {

    /**
     * Constrains the received messages to be of a given subtype.
     *
     * @tparam B the expected subtype of messages
     * @return a new matcher that expects a probe to have received messages of type `B`.
     */
    def ofSubtype[B <: A: ClassTag](implicit ev: A <:< AnyRef): FullReceiveMatcher[P, B]

    /**
     * Checks that the received message is equal to the given value.
     *
     * @param msg the expected message
     * @return a new matcher that expects a probe to have received `msg`.
     */
    def apply(msg: A): SkippableReceiveMatcher[P, A]

    /**
     * Checks that the received message satisfies a predicate or a function applying further checks.
     *
     * @param f the predicate or function applying further checks
     * @return a new matcher that expects a probe to have received messages satisfying `f`.
     */
    def which[R: AsResult](f: A => R): SkippableReceiveMatcher[P, A]

    /**
     * Checks that the received message satisfies a partial predicate or function applying further checks.
     *
     * @param f the partial predicate or function applying further checks
     * @return a new matcher that expects a probe to have received messages satisfying `f`.
     */
    def like[R: AsResult](f: PartialFunction[A, R]): SkippableReceiveMatcher[P, A]

    /**
     * Checks that the probe received a sequence of messages in order. The timeout is applied to the whole sequence and
     * not per message (i.e. all the messages have to be received before the timeout duration).
     *
     * @param msgs the expected sequence of messages
     * @return a new matcher that expects a probe to have received all the messages in `msgs` in order.
     */
    def allOf(msgs: A*): SkippableReceiveMatcher[P, Seq[A]]

    /**
     * Applies a function to the received messages before checks. Commonly used to unwrap data in envelope-like
     * messages.
     *
     * @param f the function to apply to messages
     * @tparam B the return type of the function
     * @return a new matcher that applies `f` to messages before checking them.
     */
    def unwrap[B](f: A => B): FullReceiveMatcher[P, B]

    /**
     * Applies a partial function to the received messages before checks. Messages for which the function is undefined
     * count as failures. Commonly used to unwrap data in envelope-like messages.
     *
     * @param f the partial function to apply to messages
     * @tparam B the return type of the function
     * @return a new matcher that applies `f` to messages before checking them.
     */
    def unwrapPf[B](f: PartialFunction[A, B]): FullReceiveMatcher[P, B]
  }

  trait UntypedFullReceiveMatcher[P] extends FullReceiveMatcher[P, Any] {

    /**
     * Constrains the received messages to be of a given type.
     *
     * @tparam A the expected type of messages
     * @return a new matcher that expects a probe to have received messages of type `A`.
     */
    def apply[A: ClassTag]: FullReceiveMatcher[P, A]
  }
}
