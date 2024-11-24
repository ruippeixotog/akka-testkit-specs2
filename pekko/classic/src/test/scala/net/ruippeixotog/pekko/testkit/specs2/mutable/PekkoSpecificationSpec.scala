package net.ruippeixotog.pekko.testkit.specs2.mutable

import org.apache.pekko.actor.ActorSystem
import org.specs2.concurrent.ExecutionEnv

class PekkoSpecificationSpec(implicit env: ExecutionEnv) extends PekkoSpecification {

  "A mutable PekkoSpecification" should {

    "provide an actor system, a test actor and appropriate matchers" in {
      testActor ! "hello"
      this must receive("hello")
    }

    "terminate the actor system when the spec finishes" in {
      val testSystem = ActorSystem("ShutdownTest")
      specs2.run(new PekkoSpecification(testSystem) {})
      testSystem.whenTerminated.isCompleted must beTrue
    }
  }
}
