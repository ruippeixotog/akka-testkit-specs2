package net.ruippeixotog.pekko.testkit.specs2

import scala.concurrent.duration._

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.{TestKit, TestProbe}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.ResultMatchers
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.{AfterAll, Scope}

class PekkoMatchersSpec(implicit env: ExecutionEnv)
    extends TestKit(ActorSystem())
    with SpecificationLike
    with PekkoMatchers
    with ResultMatchers
    with AfterAll {

  abstract class ProbeTest extends Scope {
    val probe = TestProbe()
    val timeout = probe.remainingOrDefault

    def schedule(msg: AnyRef, delay: FiniteDuration) =
      system.scheduler.scheduleOnce(delay, probe.ref, msg)
  }

  "PekkoMatchers" should {

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
      (probe must receive("hello")) must beFailing("Received message 'holla' but 'holla' != 'hello'")
      // no message sent
      (probe must receive("hello")) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "provide a matcher for receiving a message of a given type" in new ProbeTest {

      probe.ref ! "hello"
      (probe must receive[String]) must beSuccessful
      probe.ref ! 0.5
      (probe must receive[String]) must beFailing(
        "Received message '0.5' but '0.5: java.lang.Double' is not an instance of 'java.lang.String'"
      )
      // no message sent
      (probe must receive[String]) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "provide a matcher for receiving messages matching a function" in new ProbeTest {
      val matchTest = receive[String].which(_ must startWith("h"))

      probe.ref ! "hello"
      (probe must matchTest) must beSuccessful
      probe.ref ! "ohlla"
      (probe must matchTest) must beFailing("Received message 'ohlla' but ohlla doesn't start with 'h'")
      probe.ref ! 6
      (probe must matchTest) must beFailing(
        "Received message '6' but '6: java.lang.Integer' is not an instance of 'java.lang.String'"
      )
      // no message sent
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "provide a matcher for receiving messages matching a partial function" in new ProbeTest {
      val matchTest = receive[Option[String]].like {
        case Some(s) if s.nonEmpty => s must startWith("h")
        case None => ok
      }

      probe.ref ! Some("hello")
      (probe must matchTest) must beSuccessful
      probe.ref ! Some("ohlla")
      (probe must matchTest) must beFailing("Received message 'Some\\(ohlla\\)' but ohlla doesn't start with 'h'")
      probe.ref ! None
      (probe must matchTest) must beSuccessful
      probe.ref ! Some("")
      (probe must matchTest) must beFailing(
        "Received message 'Some\\(\\)' but undefined function for (Some\\(\\)|'Some\\(\\)')"
      )
      probe.ref ! 6
      (probe must matchTest) must beFailing(
        "Received message '6' but '6: java.lang.Integer' is not an instance of 'scala.Option'"
      )
      // no message sent
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "provide a matcher for receiving multiple unordered messages" in new ProbeTest {
      val matchTest = receive[String].allOf("a", "a", "b", "c")

      Seq("a", "a", "b", "c").foreach { probe.ref ! _ }
      (probe must matchTest) must beSuccessful
      Seq("b", "a", "c", "a").foreach { probe.ref ! _ }
      (probe must matchTest) must beSuccessful
      probe.ref ! "a"
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for messages 'a, b, c'")
      Seq("a", "b", "c").foreach { probe.ref ! _ }
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for message 'a'")
      probe.ref ! "d"
      (probe must matchTest) must beFailing("Received message 'd' but 'd' is not contained in 'a, a, b, c'")
      Seq("b", "d").foreach { probe.ref ! _ }
      (probe must matchTest) must beFailing(
        "Received message 'b' and received message 'd' but 'd' is not contained in 'a, a, c'"
      )
      Seq("a", "b", "d").foreach { probe.ref ! _ }
      (probe must matchTest) must beFailing(
        "Received messages 'a, b' and received message 'd' but 'd' is not contained in 'a, c'"
      )
      Seq("b", 6).foreach { probe.ref ! _ }
      (probe must matchTest) must beFailing(
        "Received message 'b' and received message '6' but '6: java.lang.Integer' is not an instance of 'java.lang.String'"
      )
      // no message sent
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "provide a matcher for receiving a message, discarding others meanwhile" in new ProbeTest {
      val matchTest = receive("hello").afterOthers
      val matchTestAfterOthers = receive[String].afterOthers
      val matchTestWhich = receive[String].which(_ must startWith("h")).afterOthers
      val matchTestAllOf = receive.allOf("a", "a", "b").afterOthers

      probe.ref ! "hello"
      (probe must matchTest) must beSuccessful
      Seq("ohlla", 6, "hello").foreach { probe.ref ! _ }
      (probe must matchTest) must beSuccessful
      Seq("a", "b").foreach { probe.ref ! _ }
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for matching message")
      // no message sent
      (probe must matchTest) must beFailing(s"Timeout \\($timeout\\) while waiting for matching message")

      probe.ref ! "hello"
      (probe must matchTestAfterOthers) must beSuccessful
      Seq(1, 6, "hello").foreach { probe.ref ! _ }
      (probe must matchTestAfterOthers) must beSuccessful
      Seq(1, 2).foreach { probe.ref ! _ }
      (probe must matchTestAfterOthers) must beFailing(s"Timeout \\($timeout\\) while waiting for matching message")
      // no message sent
      (probe must matchTestAfterOthers) must beFailing(s"Timeout \\($timeout\\) while waiting for matching message")

      probe.ref ! "hello"
      (probe must matchTestWhich) must beSuccessful
      Seq("ohlla", 6, "hello").foreach { probe.ref ! _ }
      (probe must matchTestWhich) must beSuccessful
      Seq("a", "b").foreach { probe.ref ! _ }
      (probe must matchTestWhich) must beFailing(s"Timeout \\($timeout\\) while waiting for matching message")
      // no message sent
      (probe must matchTestWhich) must beFailing(s"Timeout \\($timeout\\) while waiting for matching message")

      Seq("a", "a", "b").foreach { probe.ref ! _ }
      (probe must matchTestAllOf) must beSuccessful
      Seq("a", "ohlla", 6, "a", "b").foreach { probe.ref ! _ }
      (probe must matchTestAllOf) must beSuccessful
      Seq("a", "b").foreach { probe.ref ! _ }
      (probe must matchTestAllOf) must beFailing(s"Timeout \\($timeout\\) while waiting for message 'a'")
      // no message sent
      (probe must matchTestAllOf) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "allow unwrapping data in envelope-like messages using a function" in new ProbeTest {
      case class Letter(msg: String)
      val receiveLetter = receive[Letter].unwrap(_.msg)

      probe.ref ! Letter("hello")
      (probe must receiveLetter) must beSuccessful
      probe.ref ! Letter("ohlla")
      (probe must receiveLetter.which(_ must startWith("h"))) must beFailing(
        "Received message 'Letter\\(ohlla\\)' but ohlla doesn't start with 'h'"
      )
      // no message sent
      (probe must receiveLetter) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
    }

    "allow unwrapping data in envelope-like messages using a partial function" in new ProbeTest {
      case class Letter(to: String, msg: String)
      val receiveLetter = receive[Letter].unwrapPf { case Letter("john", msg) => msg }

      probe.ref ! Letter("john", "hello")
      (probe must receiveLetter) must beSuccessful
      probe.ref ! Letter("john", "ohlla")
      (probe must receiveLetter.which(_ must startWith("h"))) must beFailing(
        "Received message 'Letter\\(john,ohlla\\)' but ohlla doesn't start with 'h'"
      )
      probe.ref ! Letter("mary", "hello")
      (probe must receiveLetter) must beFailing(
        s"Received message 'Letter\\(mary,hello\\)' but undefined function for 'Letter\\(mary,hello\\)'"
      )
      // no message sent
      (probe must receiveLetter) must beFailing(s"Timeout \\($timeout\\) while waiting for message")
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
      (probe must receiveWithin(5.seconds)[String].which(_ => ok)) must beSuccessful

      schedule("a", 2.seconds)
      schedule("b", 4.seconds)
      (probe must receiveWithin(3.seconds).allOf("a", "b")) must
        beFailing(s"Timeout \\($timeout\\) while waiting for message 'b'")

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

    "work as expected with untyped function matchers and messages" in new ProbeTest {
      val matchTest = receive.like {
        case str: String => str === "str"
        case n: Int => n === 42
      }

      probe.ref ! "a"
      (probe must matchTest) must beFailing("Received message 'a' but 'a' != 'str'")
      probe.ref ! 41
      (probe must matchTest) must beFailing("Received message '41' but 41 != 42")
      probe.ref ! None
      (probe must matchTest) must beFailing("Received message 'None' but undefined function for (None|'None')")
    }
  }

  def afterAll(): Unit = shutdown()
}
