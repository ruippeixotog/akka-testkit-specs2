package net.ruippeixotog.akka.testkit.specs2

private[specs2] object Util {

  implicit class Function2Ops[A, B, R](val f: (A, B) => R) extends AnyVal {
    def andThen[S](g: R => S): (A, B) => S = { (a: A, b: B) => g(f(a, b)) }
  }

  implicit class StringCapitalizeOps[A, B, R](val str: String) extends AnyVal {
    def uncapitalize: String = {
      if (str.length == 0) ""
      else if (str.charAt(0).isLower) str
      else {
        val chars = str.toCharArray
        chars(0) = chars(0).toLower
        new String(chars)
      }
    }
  }
}
