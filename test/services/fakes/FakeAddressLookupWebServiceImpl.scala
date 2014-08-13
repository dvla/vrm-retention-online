package services.fakes

import composition.TestModule.AddressLookupServiceConstants.PostcodeValid
import models.domain.vrm_retention.{PostcodeToAddressResponse, UprnAddressPair, UprnToAddressResponse}
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.Response
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FakeAddressLookupWebServiceImpl {

  final val traderUprnValid = 12345L
  final val traderUprnValid2 = 4567L
  final val traderUprnInvalid = 66666L

  private def addressSeq(houseName: String, houseNumber: String): Seq[String] = {
    Seq(houseName, houseNumber, "property stub", "street stub", "town stub", "area stub", PostcodeValid)
  }

  def uprnAddressPairWithDefaults(uprn: String = traderUprnValid.toString, houseName: String = "presentationProperty stub", houseNumber: String = "123") =
    UprnAddressPair(uprn, address = addressSeq(houseName, houseNumber).mkString(", "))

  def postcodeToAddressResponseValid: PostcodeToAddressResponse = {
    val results = Seq(
      uprnAddressPairWithDefaults(),
      uprnAddressPairWithDefaults(uprn = "67890", houseNumber = "456"),
      uprnAddressPairWithDefaults(uprn = "111213", houseNumber = "789")
    )

    PostcodeToAddressResponse(addresses = results)
  }

  def responseValidForPostcodeToAddress: Future[Response] = {
    val inputAsJson = Json.toJson(postcodeToAddressResponseValid)

    Future {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

  def responseValidForPostcodeToAddressNotFound: Future[Response] = {
    val inputAsJson = Json.toJson(PostcodeToAddressResponse(addresses = Seq.empty))

    Future {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

  def responseWhenPostcodeInvalid = Future {
    FakeResponse(status = OK, fakeJson = None)
  }

  val uprnToAddressResponseValid = {
    val uprnAddressPair = uprnAddressPairWithDefaults()
    UprnToAddressResponse(addressViewModel = Some(AddressModel(uprn = Some(uprnAddressPair.uprn.toLong), address = uprnAddressPair.address.split(", "))))
  }

  def responseValidForUprnToAddress: Future[Response] = {
    val inputAsJson = Json.toJson(uprnToAddressResponseValid)

    Future {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }

  def responseValidForUprnToAddressNotFound: Future[Response] = {
    val inputAsJson = Json.toJson(UprnToAddressResponse(addressViewModel = None))

    Future {
      FakeResponse(status = OK, fakeJson = Some(inputAsJson))
    }
  }
}