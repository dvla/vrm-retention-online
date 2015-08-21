package models

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import views.vrm_retention.BusinessDetails.BusinessDetailsCacheKey

final case class BusinessDetailsModel(name: String, contact: String, email: String, address: AddressModel)

object BusinessDetailsModel {

  implicit val JsonFormat = Json.format[BusinessDetailsModel]
  implicit val Key = CacheKey[BusinessDetailsModel](value = BusinessDetailsCacheKey)

  def from(businessDetailsForm: SetupBusinessDetailsFormModel,
           vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): BusinessDetailsModel = {

    val businessAddress = new AddressModel(address = businessDetailsForm.toViewFormat)

    BusinessDetailsModel(
      name = businessDetailsForm.name,
      contact = businessDetailsForm.contact,
      email = businessDetailsForm.email,
      address = businessAddress
    )
  }
}