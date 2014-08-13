package models.domain.vrm_retention

import mappings.vrm_retention.BusinessDetails.BusinessDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class BusinessDetailsModel(businessName: String, businessContact: String, businessAddress: AddressModel)

object BusinessDetailsModel {

  implicit val JsonFormat = Json.format[BusinessDetailsModel]
  implicit val Key = CacheKey[BusinessDetailsModel](value = BusinessDetailsCacheKey)

  def from(setupBusinessDetailsFormModel: SetupBusinessDetailsFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           enterAddressManuallyModel: EnterAddressManuallyModel): BusinessDetailsModel = {
    val enterAddressManuallyViewModel = EnterAddressManuallyViewModel(setupBusinessDetailsFormModel, vehicleAndKeeperDetailsModel)
    val businessAddress = AddressModel.from(enterAddressManuallyModel.addressAndPostcodeModel,
      enterAddressManuallyViewModel.businessPostCode)
    BusinessDetailsModel(
      businessName = setupBusinessDetailsFormModel.businessName,
      businessContact = setupBusinessDetailsFormModel.businessContact,
      businessAddress = businessAddress)
  }
}