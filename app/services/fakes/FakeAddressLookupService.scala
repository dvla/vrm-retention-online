package services.fakes

import models.domain.disposal_of_vehicle.AddressViewModel
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import javax.inject.Inject
import services.address_lookup.{AddressLookupWebService, AddressLookupService}
import FakeWebServiceImpl.{uprnValid, uprnValid2}

/**
 * Fake implementation of the FakeAddressLookupService trait
 */
class FakeAddressLookupService @Inject()(ws: AddressLookupWebService) extends AddressLookupService {
  // TODO I think this class should no longer exist and we should have the real service call the fake webservice.
  override def fetchAddressesForPostcode(postcode: String): Future[Seq[(String, String)]] = Future {
    if (postcode == FakeAddressLookupService.postcodeInvalid) Seq.empty
    else FakeAddressLookupService.fetchedAddresses
  }

  override def fetchAddressForUprn(uprn: String): Future[Option[AddressViewModel]] = Future {
    if (uprn == FakeAddressLookupService.uprnInvalid.toString) None
    else Some(FakeAddressLookupService.address1)
  }
}

object FakeAddressLookupService {
  val uprnInvalid = 9999L
  val postcodeInvalid = "xx99xx"
  val address1 = AddressViewModel(uprn = Some(uprnValid), address = Seq("44 Hythe Road", "White City", "London", "NW10 6RJ"))
  val address2 = AddressViewModel(uprn = Some(uprnValid2), address = Seq("Penarth Road", "Cardiff", "CF11 8TT"))
  val fetchedAddresses = Seq(
    address1.uprn.getOrElse(uprnValid).toString -> address1.address.mkString(", "),
    address2.uprn.getOrElse(uprnValid2).toString -> address2.address.mkString(", ")
  )
}
