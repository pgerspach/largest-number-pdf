package model

import play.api.libs.json.{Json, OFormat}

case class LargestNumberResult(fileName: String, largestNumber: Option[Long])
object LargestNumberResult {
  implicit val format: OFormat[LargestNumberResult] = Json.format[LargestNumberResult]
}