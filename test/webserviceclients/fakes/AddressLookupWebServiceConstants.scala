package webserviceclients.fakes

import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.{PostcodeToAddressResponseDto, UprnAddressPairDto, UprnToAddressResponseDto}
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid

import scala.concurrent.Future

object AddressLookupWebServiceConstants {

  final val traderUprnValid = 12345L
  final val traderUprnValid2 = 4567L
  final val traderUprnInvalid = 66666L

  private def addressSeq(houseName: String, houseNumber: String): Seq[String] = {
    Seq(houseName, houseNumber, "property stub", "street stub", "town stub", "area stub", PostcodeValid)
  }

  def uprnAddressPairWithDefaults(uprn: String = traderUprnValid.toString, houseName: String = "presentationProperty stub", houseNumber: String = "123") =
    UprnAddressPairDto(uprn, address = addressSeq(houseName, houseNumber).mkString(", "))

  def postcodeToAddressResponseValid: PostcodeToAddressResponseDto = {
    val results = Seq(
      uprnAddressPairWithDefaults(),
      uprnAddressPairWithDefaults(uprn = "67890", houseNumber = "456"),
      uprnAddressPairWithDefaults(uprn = "111213", houseNumber = "789")
    )

    PostcodeToAddressResponseDto(addresses = results)
  }

  def responseValidForPostcodeToAddress: Future[WSResponse] = {
    val inputAsJson = Json.toJson(postcodeToAddressResponseValid)

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

  def responseValidForPostcodeToAddressNotFound: Future[WSResponse] = {
    val inputAsJson = Json.toJson(PostcodeToAddressResponseDto(addresses = Seq.empty))

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

  def responseWhenPostcodeInvalid = Future.successful {
    FakeResponse(status = OK, fakeJson = None)
  }

  def uprnToAddressResponseValid = {
    val uprnAddressPair = uprnAddressPairWithDefaults()
    UprnToAddressResponseDto(addressViewModel = Some(AddressModel(uprn = Some(uprnAddressPair.uprn.toLong), address = uprnAddressPair.address.split(", "))))
  }

  def responseValidForUprnToAddress: Future[WSResponse] = {
    val inputAsJson = Json.toJson(uprnToAddressResponseValid)

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

  def responseValidForUprnToAddressNotFound: Future[WSResponse] = {
    val inputAsJson = Json.toJson(UprnToAddressResponseDto(addressViewModel = None))

    Future.successful {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }
}
