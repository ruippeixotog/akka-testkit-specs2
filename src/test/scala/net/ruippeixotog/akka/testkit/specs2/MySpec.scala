package net.ruippeixotog.akka.testkit.specs2

import net.ruippeixotog.akka.testkit.specs2.mutable.AkkaSpecification
import scala.concurrent.duration._

import org.specs2.matcher.MatchResult

class MySpec extends AkkaSpecification {
  // testActor is not thread-safe; use `TestProbe` instances per example when possible!
  sequential

  "my test probe" should {

    "receive messages" in {
      testActor ! "hello" // expect any message
      this must receiveMessage

      testActor ! "hello" // expect a specific message
      this must receive("hello")

      testActor ! "hello" // expect a message matching a function
      this must receive.which { s: String => s must startWith("h") }

      testActor ! Some("hello") // expect a message matching a partial function
      this must receive.like[Option[String], MatchResult[_]] {
        case Some(s) => s must startWith("h")
      }

      testActor ! "b" // expect several messages, possibly unordered
      testActor ! "a"
      this must receive.allOf("a", "b")

      testActor ! "b" // expect a message (possibly not the next one)
      testActor ! "a"
      this must receive("a").afterOthers

      testActor ! "hello" // expect a message with an explicit timeout
      this must receiveWithin(1.second)("hello")

      testActor ! "hello" // ...and several combinations of the modifiers above
      this must receiveWithin(1.second).which { s: String => ok }.afterOthers
    }
  }
}
