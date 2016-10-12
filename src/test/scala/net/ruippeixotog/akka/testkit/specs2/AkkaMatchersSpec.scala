package net.ruippeixotog.akka.testkit.specs2

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import org.specs2.matcher.{ MatchResult, ResultMatchers }
import org.specs2.mutable.SpecificationLike
import scala.concurrent.duration._

import org.specs2.concurrent.ExecutionEnv
import org.specs2.specification.{ AfterAll, Scope }

class AkkaMatchersSpec(implicit env: ExecutionEnv) extends TestKit(ActorSystem()) with SpecificationLike
    with AkkaMatchers with ResultMatchers with AfterAll {

  val timeout = remainingOrDefault

  abstract class ProbeTest extends Scope {
    val probe = TestProbe()

    def schedule(msg: AnyRef, delay: FiniteDuration) =
      system.scheduler.scheduleOnce(delay, probe.ref, msg)
  }

  "AkkaMatchers" should {

    "provide a matcher for receiving any message" in new ProbeTest {

      probe.ref ! "hello"
      (probe must receive) must beSuccessful
      // no message sent
      (probe must receive) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "provide a matcher for receiving a specific message" in new ProbeTest {

      probe.ref ! "hello"
      (probe must receive("hello")) must beSuccessful
      probe.ref ! "holla"
      (probe must receive("hello")) must beFailing("Received message 'holla' but 'holla' is not equal to 'hello'")
      // no message sent
      (probe must receive("hello")) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "provide a matcher for receiving messages matching a function" in new ProbeTest {
      val matchTest = receive.which { s: String => s must startWith("h") }

      probe.ref ! "hello"
      (probe must matchTest) must beSuccessful
      probe.ref ! "ohlla"
      (probe must matchTest) must beFailing("Received message 'ohlla' but 'ohlla' doesn't start with 'h'")
      probe.ref ! 6
      (probe must matchTest) must beFailing(
        "Received message '6' but '6: java.lang.Integer' is not an instance of 'java.lang.String'")
      // no message sent
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "provide a matcher for receiving messages matching a partial function" in new ProbeTest {
      val matchTest = receive.like[Option[String], MatchResult[_]] {
        case Some(s) if s.nonEmpty => s must startWith("h")
        case None => ok
      }

      probe.ref ! Some("hello")
      (probe must matchTest) must beSuccessful
      probe.ref ! Some("ohlla")
      (probe must matchTest) must beFailing("Received message 'Some\\(ohlla\\)' but 'ohlla' doesn't start with 'h'")
      probe.ref ! None
      (probe must matchTest) must beSuccessful
      probe.ref ! Some("")
      (probe must matchTest) must beFailing("Received message 'Some\\(\\)' but undefined function for 'Some\\(\\)'")
      probe.ref ! 6
      (probe must matchTest) must beFailing(
        "Received message '6' but '6: java.lang.Integer' is not an instance of 'scala.Option'")
      // no message sent
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "provide a matcher for receiving multiple unordered messages" in new ProbeTest {
      val matchTest = receive.allOf("a", "a", "b")

      Seq("a", "a", "b").foreach { probe.ref ! _ }
      (probe must matchTest) must beSuccessful
      Seq("b", "a", "a").foreach { probe.ref ! _ }
      (probe must matchTest) must beSuccessful
      probe.ref ! "a"
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for messages 'a', 'b'")
      Seq("a", "b").foreach { probe.ref ! _ }
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for messages 'a'")
      Seq("b", 6).foreach { probe.ref ! _ }
      (probe must matchTest) must beFailing("Received unexpected message '6'")
      // no message sent
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "provide a matcher for receiving a message, discarding others meanwhile" in new ProbeTest {
      val matchTest = receive("hello").afterOthers
      val matchTestWhich = receive.which { s: String => s must startWith("h") }.afterOthers

      probe.ref ! "hello"
      (probe must matchTest) must beSuccessful
      Seq("ohlla", 6, "hello").foreach { probe.ref ! _ }
      (probe must matchTest) must beSuccessful
      Seq("a", "b").foreach { probe.ref ! _ }
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for matching message")
      // no message sent
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for matching message")

      probe.ref ! "hello"
      (probe must matchTestWhich) must beSuccessful
      Seq("ohlla", 6, "hello").foreach { probe.ref ! _ }
      (probe must matchTestWhich) must beSuccessful
      Seq("a", "b").foreach { probe.ref ! _ }
      (probe must matchTestWhich) must beFailing(s"Timeout \\($timeout\\) while waiting for matching message")
      // no message sent
      (probe must matchTestWhich) must beFailing(s"Timeout \\($timeout\\) while waiting for matching message")
    }

    "allow defining a custom timeout for all the matchers" in new ProbeTest {

      schedule("hello", 3.seconds)
      (probe must receiveWithin(2.second)) must beFailing(s"Didn't receive any message within 2 seconds")
      (probe must receiveWithin(2.second)) must beSuccessful

      schedule("hello", 5.seconds)
      (probe must receiveWithin(1.second)("hello")) must beFailing(s"Didn't receive any message within 1 second")
      (probe must receiveWithin(2.second)("hello")) must beFailing(s"Didn't receive any message within 2 seconds")
      (probe must receiveWithin(3.second)("hello")) must beSuccessful

      schedule("hello", 4.seconds)
      (probe must receiveWithin(5.seconds).which { s: String => ok }) must beSuccessful

      schedule("a", 2.seconds)
      schedule("b", 4.seconds)
      (probe must receiveWithin(3.seconds).allOf("a", "b")) must
        beFailing(s"Timeout \\($timeout\\) while waiting for messages 'b'")

      (probe must receiveWithin(2.seconds)("b")) must beSuccessful

      schedule("a", 2.seconds)
      schedule("b", 4.seconds)
      (probe must receiveWithin(3.seconds)("b").afterOthers) must
        beFailing(s"Timeout \\($timeout\\) while waiting for matching message")

      (probe must receiveWithin(2.seconds)("b")) must beSuccessful
    }

    "define receiveMessage* matchers as synonyms for receive*" in new ProbeTest {
      probe.ref ! "hello"
      (probe must receiveMessage("hello")) must beSuccessful

      schedule("hello", 4.seconds)
      (probe must receiveMessageWithin(5.seconds)("hello")) must beSuccessful
    }
  }

  def afterAll() = shutdown()
}
