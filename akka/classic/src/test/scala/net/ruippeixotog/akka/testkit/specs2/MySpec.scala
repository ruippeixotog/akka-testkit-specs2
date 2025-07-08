package net.ruippeixotog.akka.testkit.specs2

import scala.concurrent.duration._

import net.ruippeixotog.akka.testkit.specs2.mutable.AkkaSpecification
import akka.testkit.TestKitBase

class MySpec extends AkkaSpecification {
  // testActor is not thread-safe; use `TestProbe` instances per example when possible!
  sequential

  "my test probe" should {

    "receive messages" in {
      testActor ! "hello" // expect any message
      (this: TestKitBase) must receiveMessage

      testActor ! "hello" // expect a specific message
      (this: TestKitBase) must receive("hello")

      testActor ! "hello" // expect a message of a given type
      (this: TestKitBase) must receive[String]

      testActor ! "hello" // expect a message matching a function
      (this: TestKitBase) must receive[String].which(_ must startWith("h"))

      testActor ! Some("hello") // expect a message matching a partial function
      (this: TestKitBase) must receive.like { case Some(s: String) =>
        s must startWith("h")
      }

      testActor ! "b" // expect several messages, possibly unordered
      testActor ! "a"
      (this: TestKitBase) must receive.allOf("a", "b")

      testActor ! "b" // expect a message (possibly not the next one)
      testActor ! "a"
      (this: TestKitBase) must receive("a").afterOthers

      testActor ! "hello" // expect a message with an explicit timeout
      (this: TestKitBase) must receiveWithin(1.second)("hello")

      case class Envelope(msg: String) // unwrap a message before matching
      testActor ! Envelope("hello")
      (this: TestKitBase) must receive[Envelope].unwrap(_.msg).which(_ must startWith("h"))

      testActor ! "hello" // ...and several combinations of the modifiers above
      (this: TestKitBase) must receiveWithin(1.second)[String].which(_ must startWith("h")).afterOthers
    }
  }
}
