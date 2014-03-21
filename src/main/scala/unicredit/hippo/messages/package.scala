package unicredit.hippo


package object messages {
  case class Retrieve(
    table: String,
    keys: List[String],
    columns: List[String]
  )
  case class Result(content: Map[String, Map[String, String]])
  case class Switch(version: String)
}