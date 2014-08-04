package models.domain.vrm_retention

import constraints.common.Postcode.formatPostcode
import constraints.common.RegistrationNumber.formatVrm
import mappings.vrm_retention.VehicleLookup.VehicleAndKeeperLookupDetailsCacheKey
import models.domain.common.{AddressAndPostcodeModel, AddressLinesModel, AddressViewModel, CacheKey}
import play.api.libs.json.Json

final case class VehicleAndKeeperDetailsModel(registrationNumber: String,
                                              vehicleMake: Option[String],
                                              vehicleModel: Option[String],
                                              keeperTitle: Option[String],
                                              keeperFirstName: Option[String],
                                              keeperLastName: Option[String],
                                              keeperAddress: Option[AddressViewModel])

object VehicleAndKeeperDetailsModel {

  // Create a VehicleAndKeeperDetailsDto from the given replacementVRM. We do this in order get the data out of the response from micro-service call
  def fromDto(vehicleAndKeeperDetailsDto: VehicleAndKeeperDetailsDto) = {

    val addressViewModel = {
      val addressLineModel = AddressLinesModel(vehicleAndKeeperDetailsDto.keeperAddressLine1.get,
        vehicleAndKeeperDetailsDto.keeperAddressLine2,
        vehicleAndKeeperDetailsDto.keeperAddressLine3,
        vehicleAndKeeperDetailsDto.keeperAddressLine4,
        vehicleAndKeeperDetailsDto.keeperPostTown.get)
      val addressAndPostcodeModel = AddressAndPostcodeModel(None, addressLineModel)
      AddressViewModel.from(addressAndPostcodeModel, formatPostcode(vehicleAndKeeperDetailsDto.keeperPostcode.get))
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