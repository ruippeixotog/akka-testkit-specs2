package net.ruippeixotog.akka.testkit.specs2

private[specs2] object Util {

  implicit class Function2Ops[A, B, R](val f: (A, B) => R) {
    def andThen[S](g: R => S): (A, B) => S = { (a: A, b: B) => g(f(a, b)) }
  }
}
