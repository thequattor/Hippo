package unicredit.hippo.storage


trait Joiner {
  def join(a: String, b: String): String
  def split(x: String): (String, String)
}

class TrivialJoiner(separator: String) extends Joiner {
  def join(key: String, column: String) =
    s"$key$separator$column"

  def split(x: String) = x split separator match {
    case Array(a, b) => (a, b)
    case _ => sys.error(s"Not a valid encoded string: $x")
  }

}