package mappings.vrm_retention

import play.api.data.Mapping
import play.api.data.Forms._

object KeeperConsent {
  def keeperConsent: Mapping[String] = nonEmptyText
}
