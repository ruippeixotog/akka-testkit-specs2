package net.ruippeixotog.akka.testkit.specs2.mutable

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import org.specs2.concurrent.ExecutionEnv

class AkkaTypedSpecificationSpec(implicit env: ExecutionEnv) extends AkkaTypedSpecification {

  "A mutable AkkaTypedSpecification" should {

    "provide a testkit and appropriate matchers" in {
      val probe = testKit.createTestProbe[String]()

      probe.ref ! "hello"
      probe must receive("hello")
    }

    "terminate the testkit when the spec finishes" in {
      val testKit = ActorTestKit()
      specs2.run(new AkkaTypedSpecification(testKit) {})
      testKit.system.whenTerminated.isCompleted must beTrue
    }
  }
}
