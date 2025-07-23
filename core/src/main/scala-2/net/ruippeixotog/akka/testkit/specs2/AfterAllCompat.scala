package net.ruippeixotog.akka.testkit.specs2

import org.specs2.specification.AfterAll

trait AfterAllCompat extends AfterAll {
  def shutdownTestkit(): Unit

  def afterAll(): Unit = shutdownTestkit()
}
