package net.ruippeixotog.pekko.testkit.specs2

import net.ruippeixotog.pekko.testkit.specs2.mutable.PekkoTypedSpecification

class MyTypedSpec extends PekkoTypedSpecification {

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
