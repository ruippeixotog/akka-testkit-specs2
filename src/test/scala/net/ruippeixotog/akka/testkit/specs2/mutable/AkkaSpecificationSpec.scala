package net.ruippeixotog.akka.testkit.specs2.mutable

import akka.actor.ActorSystem
import org.specs2.concurrent.ExecutionEnv

class AkkaSpecificationSpec(implicit env: ExecutionEnv) extends AkkaSpecification {

  "A mutable AkkaSpecification" should {

    "provide an actor system, a test actor and appropriate matchers" in {
      testActor ! "hello"
      this must receive("hello")
    }

    "terminate the actor system when the spec finishes" in {
      val testSystem = ActorSystem("ShutdownTest")
      specs2.run(new AkkaSpecification(testSystem))
      testSystem.whenTerminated.isCompleted must beTrue
    }
  }
}
