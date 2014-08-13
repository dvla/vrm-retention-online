package mappings.common

import play.api.data.Forms.{number, optional}
import play.api.data.Mapping

// TODO move to common project except for the ID
object Uprn {
  final val UprnId = "uprn"

  def uprn: Mapping[Option[Int]] = optional(number)
}