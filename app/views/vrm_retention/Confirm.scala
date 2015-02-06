package views.vrm_retention

import models.CacheKeyPrefix
import play.api.data.Forms._
import play.api.data.Mapping

object Confirm {

  final val KeeperEmailMaxLength = 254
  final val SummaryId = "summary"
  final val ConfirmId = "confirm"
  final val ExitId = "exit"
  final val KeeperEmailId = "keeper-email"
  final val ConfirmCacheKey = s"${CacheKeyPrefix}confirm"
  final val SupplyEmailId = "supply-email"
  final val SupplyEmail_true = "true"
  final val SupplyEmail_false = "false"
  final val KeeperEmailWrapper = "keeper-email-wrapper"

  def supplyEmail: Mapping[String] = nonEmptyText
}