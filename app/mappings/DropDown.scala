package mappings

import play.api.data.Forms.nonEmptyText
import play.api.data.Mapping

// TODO move to common project
object DropDown {
  def addressDropDown: Mapping[String] = nonEmptyText
}