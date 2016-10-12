# akka-testkit-specs2

A small library for those who use [akka-testkit](http://doc.akka.io/docs/akka/current/scala/testing.html) in [specs2](http://etorreborre.github.io/specs2/) specifications. Provides idiomatic specs2 matchers for checking the correct reception of messages by test actors and probes, handling the provision and proper termination of test actor systems.

## Usage

To use akka-testkit-specs2 in an existing SBT project with Scala 2.11.x, add the following dependency to your `build.sbt`:

```scala
libraryDependencies += "net.ruippeixotog" %% "akka-testkit-sepcs2" % "0.1.0-SNAPSHOT"
```

To use it in your specifications, just extend `AkkaSpecification`:

```scala
class MySpec extends AkkaSpecification {

  "MySpec" should {

    "provide a test actor and matchers for messages" in {
      testActor ! "hello"
      this must receive("hello")
    }
  }
}
```

If you can't extend classes in your specification, just mix-in the trait `AkkaSpecificationLike` (you just need to provide it an `ActorSystem`). If you only need the matchers, you can also mix-in directly `AkkaMatchers`. While `AkkaSpecification` and `AkkaSpecificationLike` only support mutable specifications, `AkkaMatchers` should work in immutable specifications too.

The testkit provides several ways to check messages received by a test actor or a test probe. This library provides expectations already existent in akka-testkit `TestKitBase` in the form of specs2 `Matchers` with proper failure messages and an idiomatic syntax:

```scala
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

      testActor ! "b" // expect a message (possible not the next one)
      testActor ! "a"
      this must receive("a").afterOthers

      testActor ! "hello" // expect a message with an explicit timeout
      this must receiveWithin(1.second)("hello")

      testActor ! "hello" // ...and several combinations of the modifiers above
      this must receiveWithin(1.second).which { s: String => ok }.afterOthers
    }
  }
}
```

## Copyright

Copyright (c) 2016 Rui Gonçalves. See LICENSE for details.
