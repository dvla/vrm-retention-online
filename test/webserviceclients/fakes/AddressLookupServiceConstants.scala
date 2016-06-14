package webserviceclients.fakes

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel

object AddressLookupServiceConstants {

  final val TraderBusinessNameValid = "example trader name"
  final val TraderBusinessContactValid = "example trader contact"
  final val TraderBusinessEmailValid = "business.example@test.com"

  final val KeeperEmailValid = Some("keeper.example@test.com")

  final val PostcodeInvalid = "xx99xx"
  final val PostcodeValid = "QQ99QQ"
  final val PostTownValid = "postTown stub"
  final val BusinessAddressLine1Valid = "business line1 stub"
  final val BusinessAddressLine2Valid = "business line2 stub"
  final val BusinessAddressLine3Valid: String = ""
  final val BusinessAddressPostTownValid = "business postTown stub"
  final val SearchPostcodeValid: String = "AA11AA"
  final val AddressListSelectValid: String = "1"

  final val addressWithoutUprn = AddressModel(
    address = Seq(BusinessAddressLine1Valid, BusinessAddressLine2Valid, BusinessAddressPostTownValid, PostcodeValid)
  )

}