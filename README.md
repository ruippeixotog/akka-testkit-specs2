# akka-testkit-specs2 [![Build Status](https://travis-ci.org/ruippeixotog/akka-testkit-specs2.svg?branch=master)](https://travis-ci.org/ruippeixotog/akka-testkit-specs2) [![Coverage Status](https://coveralls.io/repos/github/ruippeixotog/akka-testkit-specs2/badge.svg?branch=master)](https://coveralls.io/github/ruippeixotog/akka-testkit-specs2?branch=master)

A small library for those who use [akka-testkit](http://doc.akka.io/docs/akka/current/scala/testing.html) in [specs2](http://etorreborre.github.io/specs2/) specifications. Provides idiomatic specs2 matchers for checking the correct reception of messages by test actors and probes, handling the provision and proper termination of test actor systems.

## Usage

To use akka-testkit-specs2 in an existing SBT project with Scala 2.12 or 2.13, add the following dependency to your `build.sbt`:

```scala
libraryDependencies += "net.ruippeixotog" %% "akka-testkit-specs2" % "0.3.1"
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

      testActor ! "hello" // expect a message of a given type
      this must receive[String]

      testActor ! "hello" // expect a message matching a function
      this must receive[String].which(_ must startWith("h"))

      testActor ! Some("hello") // expect a message matching a partial function
      this must receive.like {
        case Some(s: String) => s must startWith("h")
      }

      testActor ! "b" // expect several messages, possibly unordered
      testActor ! "a"
      this must receive.allOf("a", "b")

      testActor ! "b" // expect a message (possibly not the next one)
      testActor ! "a"
      this must receive("a").afterOthers

      testActor ! "hello" // expect a message with an explicit timeout
      this must receiveWithin(1.second)("hello")

      case class Envelope(msg: String) // unwrap a message before matching
      testActor ! Envelope("hello")
      this must receive[Envelope].unwrap(_.msg).which(_ must startWith("h"))

      testActor ! "hello" // ...and several combinations of the modifiers above
      this must receiveWithin(1.second)[String].which(_ must startWith("h")).afterOthers
    }
  }
}
```

## Akka Typed Actors

If you're using the new [Typed Actor API](https://doc.akka.io/docs/akka/current/typed/actors.html) avalilable since Akka 2.6, you can use `AkkaTypedSpecification`, `AkkaTypedSpecificationLike` and `AkkaTypedMatchers` instead of the traits above:

```scala
class MyTypedSpec extends AkkaTypedSpecification {

  "my typed test probe" should {

    "receive messages" in {
      val probe = testKit.createTestProbe[String]()

      probe.ref ! "hello" // expect any message
      probe must receiveMessage

      probe.ref ! "hello" // expect a specific message
      probe must receive("hello")

      // any of the following are type errors:
      // probe.ref ! 3
      // probe must receive(3)

      // (...)
    }
  }
}
```

## Copyright

Copyright (c) 2016-2020 Rui Gonçalves. See LICENSE for details.
