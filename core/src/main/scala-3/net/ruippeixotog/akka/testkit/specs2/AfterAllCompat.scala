package net.ruippeixotog.akka.testkit.specs2

import org.specs2.specification.AfterSpec
import org.specs2.specification.core.Fragments
import org.specs2.mutable.SpecificationLike

trait AfterAllCompat extends AfterSpec { self: SpecificationLike =>
  def shutdownTestkit(): Unit

  def afterSpec: Fragments = step(shutdownTestkit())
}
