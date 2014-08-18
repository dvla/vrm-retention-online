package views.vrm_retention

import play.api.data.Forms._
import play.api.data.Mapping

object KeeperConsent {

  def keeperConsent: Mapping[String] = nonEmptyText
}