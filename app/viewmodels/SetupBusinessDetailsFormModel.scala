package viewmodels

import play.api.data.Forms.mapping
import play.api.data.validation.Constraints
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.BusinessName
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.email
import uk.gov.dvla.vehicles.presentation.common.mappings.Postcode._
import views.vrm_retention.SetupBusinessDetails._

final case class SetupBusinessDetailsFormModel(name: String, contact: String, email: String, postcode: String)

object SetupBusinessDetailsFormModel {

  implicit val JsonFormat = Json.format[SetupBusinessDetailsFormModel]
  implicit val Key = CacheKey[SetupBusinessDetailsFormModel](SetupBusinessDetailsCacheKey)

  object Form {

    final val Mapping = mapping(
      BusinessNameId -> BusinessName.businessNameMapping,
      BusinessContactId -> BusinessName.businessNameMapping,
      BusinessEmailId -> email.verifying(Constraints.nonEmpty),
      BusinessPostcodeId -> postcode
    )(SetupBusinessDetailsFormModel.apply)(SetupBusinessDetailsFormModel.unapply)
  }

}