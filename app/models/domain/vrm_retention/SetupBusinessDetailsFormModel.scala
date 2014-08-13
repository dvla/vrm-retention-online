package models.domain.vrm_retention

import mappings.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

// TODO the names of the params repeat names from the model so refactor
final case class SetupBusinessDetailsFormModel(businessName: String, businessContact: String, businessEmail: String, businessPostcode: String)

object SetupBusinessDetailsFormModel {

  implicit val JsonFormat = Json.format[SetupBusinessDetailsFormModel]
  implicit val Key = CacheKey[SetupBusinessDetailsFormModel](SetupBusinessDetailsCacheKey)
}