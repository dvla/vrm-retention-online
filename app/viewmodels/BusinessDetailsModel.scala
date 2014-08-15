package viewmodels

import views.vrm_retention.BusinessDetails
import BusinessDetails.BusinessDetailsCacheKey
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

final case class BusinessDetailsModel(name: String, contact: String, email: String, address: AddressModel)

object BusinessDetailsModel {

  implicit val JsonFormat = Json.format[BusinessDetailsModel]
  implicit val Key = CacheKey[BusinessDetailsModel](value = BusinessDetailsCacheKey)

  def from(setupBusinessDetailsFormModel: SetupBusinessDetailsFormModel,
           vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
           enterAddressManuallyModel: EnterAddressManuallyModel): BusinessDetailsModel = {
    val enterAddressManuallyViewModel = EnterAddressManuallyViewModel(setupBusinessDetailsFormModel, vehicleAndKeeperDetailsModel)
    val businessAddress = AddressModel.from(enterAddressManuallyModel.addressAndPostcodeViewModel,
      enterAddressManuallyViewModel.businessPostCode)
    BusinessDetailsModel(
      name = setupBusinessDetailsFormModel.name,
      contact = setupBusinessDetailsFormModel.contact,
      email = setupBusinessDetailsFormModel.email,
      address = businessAddress)
  }
}