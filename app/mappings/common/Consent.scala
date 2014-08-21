package mappings.common

import play.api.data.Forms.default
import play.api.data.Forms.text
import play.api.data.Mapping

object Consent {
  def consent: Mapping[String] = default(text, "")
}