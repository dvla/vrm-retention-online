package mappings.common.vrm_retention

import play.api.data.Mapping
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import views.constraints.Postcode.validPostcode

object Postcode {
  private final val MinLength = 0
  final val MaxLength = 8

  def postcode: Mapping[String] = {
    nonEmptyTextWithTransform(_.toUpperCase.trim)(MinLength, MaxLength) verifying validPostcode
  }
}