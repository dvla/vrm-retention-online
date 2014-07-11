package services.fakes

import services.fakes.FakeAddressLookupWebServiceImpl.{traderUprnValid, traderUprnValid2}
import models.domain.common.AddressViewModel

object FakeAddressLookupService {
  final val TraderBusinessNameValid = "example trader name"
  final val PostcodeInvalid = "xx99xx"
  final val PostcodeValid = "QQ99QQ"
  val addressWithoutUprn = AddressViewModel(address = Seq("44 Hythe Road", "White City", "London", PostcodeValid))
  val addressWithUprn = AddressViewModel(
    uprn = Some(traderUprnValid),
    address = Seq("44 Hythe Road", "White City", "London", PostcodeValid)
  )
  final val BuildingNameOrNumberValid = "1234"
  final val Line2Valid = "line2 stub"
  final val Line3Valid = "line3 stub"
  final val PostTownValid = "postTown stub"

  final val PostcodeValidWithSpace = "QQ9 9QQ"
  final val PostcodeNoResults = "SA99 1DD"
  val fetchedAddresses = Seq(
    traderUprnValid.toString -> addressWithUprn.address.mkString(", "),
    traderUprnValid2.toString -> addressWithUprn.address.mkString(", ")
  )
}