package viewmodels

import uk.gov.dvla.vehicles.presentation.common.views.constraints.Postcode.formatPostcode
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm
import views.vrm_retention.VehicleLookup
import VehicleLookup.VehicleAndKeeperLookupDetailsCacheKey
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.views.models.{AddressAndPostcodeViewModel, AddressLinesViewModel}
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey

final case class VehicleAndKeeperDetailsModel(registrationNumber: String,
                                              vehicleMake: Option[String],
                                              vehicleModel: Option[String],
                                              keeperTitle: Option[String],
                                              keeperFirstName: Option[String],
                                              keeperLastName: Option[String],
                                              keeperAddress: Option[AddressModel])

object VehicleAndKeeperDetailsModel {

  // Create a VehicleAndKeeperDetailsDto from the given replacementVRM. We do this in order get the data out of the response from micro-service call
  def from(vehicleAndKeeperDetailsDto: VehicleAndKeeperDetailsDto) = {

    val addressViewModel = {
      val addressLineModel = AddressLinesViewModel(vehicleAndKeeperDetailsDto.keeperAddressLine1.get,
        vehicleAndKeeperDetailsDto.keeperAddressLine2,
        vehicleAndKeeperDetailsDto.keeperAddressLine3,
        vehicleAndKeeperDetailsDto.keeperAddressLine4,
        vehicleAndKeeperDetailsDto.keeperPostTown.get)
      val addressAndPostcodeModel = AddressAndPostcodeViewModel(None, addressLineModel)
      AddressModel.from(addressAndPostcodeModel, formatPostcode(vehicleAndKeeperDetailsDto.keeperPostcode.get))
    }

    VehicleAndKeeperDetailsModel(registrationNumber = formatVrm(vehicleAndKeeperDetailsDto.registrationNumber),
      vehicleMake = vehicleAndKeeperDetailsDto.vehicleMake,
      vehicleModel = vehicleAndKeeperDetailsDto.vehicleModel,
      keeperTitle = vehicleAndKeeperDetailsDto.keeperTitle,
      keeperFirstName = vehicleAndKeeperDetailsDto.keeperFirstName,
      keeperLastName = vehicleAndKeeperDetailsDto.keeperLastName,
      keeperAddress = Some(addressViewModel))
  }

  implicit val JsonFormat = Json.format[VehicleAndKeeperDetailsModel]
  implicit val Key = CacheKey[VehicleAndKeeperDetailsModel](VehicleAndKeeperLookupDetailsCacheKey)
}