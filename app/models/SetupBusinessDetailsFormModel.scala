package models

import play.api.data.Forms.mapping
import play.api.data.validation.Constraints
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.mappings.{AddressPicker, BusinessName}
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.emailConfirm
import uk.gov.dvla.vehicles.presentation.common.model.{SearchFields, Address}
import views.vrm_retention.SetupBusinessDetails.BusinessAddressId
import views.vrm_retention.SetupBusinessDetails.BusinessContactId
import views.vrm_retention.SetupBusinessDetails.BusinessEmailId
import views.vrm_retention.SetupBusinessDetails.BusinessNameId
import views.vrm_retention.SetupBusinessDetails.SetupBusinessDetailsCacheKey

final case class SetupBusinessDetailsFormModel(name: String, contact: String, email: String, address: Address)

object SetupBusinessDetailsFormModel {

  implicit val searchFieldsFormat = Json.format[SearchFields]
  implicit val addressFormat = Json.format[Address]
  implicit val JsonFormat = Json.format[SetupBusinessDetailsFormModel]
  implicit val Key = CacheKey[SetupBusinessDetailsFormModel](SetupBusinessDetailsCacheKey)

  object Form {

    final val Mapping = mapping(
      BusinessNameId -> BusinessName.businessNameMapping,
      BusinessContactId -> BusinessName.businessNameMapping,
      BusinessEmailId -> emailConfirm.verifying(Constraints.nonEmpty),
      BusinessAddressId -> AddressPicker.mapAddress
    )(SetupBusinessDetailsFormModel.apply)(SetupBusinessDetailsFormModel.unapply)
  }
}
