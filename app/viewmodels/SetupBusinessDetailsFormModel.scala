package viewmodels

import mappings.vrm_retention.SetupBusinessDetails._
import play.api.libs.json.Json
import play.api.data.Forms._
import play.api.data.validation.Constraints
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Postcode._

// TODO the names of the params repeat names from the model so refactor
final case class SetupBusinessDetailsFormModel(businessName: String, businessContact: String, businessEmail: String, businessPostcode: String)

object SetupBusinessDetailsFormModel {

  implicit val JsonFormat = Json.format[SetupBusinessDetailsFormModel]
  implicit val Key = CacheKey[SetupBusinessDetailsFormModel](SetupBusinessDetailsCacheKey)

  object Form {
    final val Mapping = mapping(
      BusinessNameId -> businessName(),
      BusinessContactId -> businessContact(),
      BusinessEmailId -> email.verifying(Constraints.nonEmpty),
      BusinessPostcodeId -> postcode
    )(SetupBusinessDetailsFormModel.apply)(SetupBusinessDetailsFormModel.unapply)
  }
}