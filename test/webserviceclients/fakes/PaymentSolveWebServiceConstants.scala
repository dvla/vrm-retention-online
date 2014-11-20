package webserviceclients.fakes

object PaymentSolveWebServiceConstants {

  final val TransactionReferenceValid = Some("1q2w3e4r5t6y7u8i9o0p")
  final val MaskedPANValid = Some("1234********1234")
  final val AuthCodeValid = Some("D12345")
  final val MerchantIdValid = Some("123321123")
  final val PaymentTypeValid = Some("Card")
  final val CardTypeValid = Some("V")
  final val TotalAmountPaidValid = Some(8000L)

  //  private val vehicleAndKeeperDetails = VehicleAndKeeperDetailsDto(registrationNumber = RegistrationNumberValid,
  //    vehicleMake = VehicleMakeValid,
  //    vehicleModel = VehicleModelValid,
  //    keeperTitle = KeeperTitleValid,
  //    keeperFirstName = KeeperFirstNameValid,
  //    keeperLastName = KeeperLastNameValid,
  //    keeperAddressLine1 = KeeperAddressLine1Valid,
  //    keeperAddressLine2 = KeeperAddressLine2Valid,
  //    keeperAddressLine3 = KeeperAddressLine3Valid,
  //    keeperAddressLine4 = KeeperAddressLine4Valid,
  //    keeperPostTown = KeeperPostTownValid,
  //    keeperPostcode = KeeperPostCodeValid
  //  )
  //
  //  val vehicleAndKeeperDetailsResponseSuccess: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
  //    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = None, vehicleAndKeeperDetailsDto = Some(vehicleAndKeeperDetails))))
  //  }
  //
  //  val vehicleAndKeeperDetailsResponseVRMNotFound: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
  //    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = Some("vehicle_lookup_vrm_not_found"), vehicleAndKeeperDetailsDto = None)))
  //  }
  //
  //  val vehicleAndKeeperDetailsResponseDocRefNumberNotLatest: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
  //    (OK, Some(VehicleAndKeeperDetailsResponse(
  //      responseCode = Some("vehicle_and_keeper_lookup_document_record_mismatch"),
  //      vehicleAndKeeperDetailsDto = None
  //    )))
  //  }
  //
  //  val vehicleAndKeeperDetailsResponseNotFoundResponseCode: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
  //    (OK, Some(VehicleAndKeeperDetailsResponse(responseCode = None, vehicleAndKeeperDetailsDto = None)))
  //  }
  //
  //  val vehicleAndKeeperDetailsServerDown: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
  //    (SERVICE_UNAVAILABLE, None)
  //  }
  //
  //  val vehicleAndKeeperDetailsNoResponse: (Int, Option[VehicleAndKeeperDetailsResponse]) = {
  //    (OK, None)
  //  }
}
