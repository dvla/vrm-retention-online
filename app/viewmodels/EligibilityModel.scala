package viewmodels

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm
import views.vrm_retention.CheckEligibility.CheckEligibilityCacheKey

final case class EligibilityModel(replacementVRM: String)

object EligibilityModel {

  // Create a EligibilityModel from the given replacementVRM. We do this in order get the data out of the response from micro-service call
  def from(replacementVRM: String) = EligibilityModel(replacementVRM = formatVrm(replacementVRM))

  implicit val JsonFormat = Json.format[EligibilityModel]
  implicit val Key = CacheKey[EligibilityModel](CheckEligibilityCacheKey)
}