package viewmodels

import views.vrm_retention.SetupBusinessDetails._
import mappings.common.Email.email
import play.api.data.Forms.mapping
import play.api.data.validation.Constraints
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.Postcode._

final case class SetupBusinessDetailsFormModel(name: String, contact: String, email: String, postcode: String)

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