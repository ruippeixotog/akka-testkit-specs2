package net.ruippeixotog.akka.testkit.specs2

import org.specs2.mutable.Specification

import net.ruippeixotog.akka.testkit.specs2.Util._

class UtilSpec extends Specification {

  "The Util object" should {

    "proide a method `andThen` to Function2 instances" in {
      val take = { (str: String, n: Int) => str.take(n) }
      val length = { (str: String) => str.length }
      val takeLength = take.andThen(length)

      takeLength("abcde", 2) mustEqual 2
      takeLength("abcde", 10) mustEqual 5
      takeLength("", 2) mustEqual 0
    }

    "provide a method `uncapitalize` to strings" in {
      "".uncapitalize mustEqual ""
      "A".uncapitalize mustEqual "a"
      "One method".uncapitalize mustEqual "one method"
      "this is lowercase".uncapitalize mustEqual "this is lowercase"
      "¿que?".uncapitalize mustEqual "¿que?"
    }
  }
}
