package services.address_lookup.ordnance_survey

import common.ClearTextClientSideSessionFactory
import helpers.UnitSpec
import models.domain.disposal_of_vehicle.{UprnToAddressResponse, PostcodeToAddressResponse}
import play.api.http.Status.{OK, NOT_FOUND}
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import play.api.libs.ws.Response
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import services.address_lookup.AddressLookupService
import services.fakes.FakeAddressLookupService.PostcodeValid
import services.fakes.FakeAddressLookupWebServiceImpl
import services.fakes.FakeAddressLookupWebServiceImpl.postcodeToAddressResponseValid
import services.fakes.FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress
import services.fakes.FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress
import services.fakes.FakeAddressLookupWebServiceImpl.traderUprnValid
import services.fakes.FakeAddressLookupWebServiceImpl.uprnToAddressResponseValid
import services.fakes.FakeResponse

final class OSAddressLookupServiceSpec extends UnitSpec {

  "fetchAddressesForPostcode" should {
    "return seq when response status is 200 OK and returns results" in {
      val service = addressServiceMock(responseValidForPostcodeToAddress)

      val result = service.fetchAddressesForPostcode(PostcodeValid, ClearTextClientSideSessionFactory.DefaultTrackingId)

      whenReady(result, timeout) {
        r =>
          r.length should equal(postcodeToAddressResponseValid.addresses.length)
          r should equal(postcodeToAddressResponseValid.addresses.map(i => (i.uprn, i.address)))
      }
    }

    "return empty seq when response status is Ok but results is empty" in {
      val service = addressServiceMock(responsePostcode(OK, PostcodeToAddressResponse(addresses = Seq.empty)))

      val result = service.fetchAddressesForPostcode(PostcodeValid, ClearTextClientSideSessionFactory.DefaultTrackingId)

      whenReady(result, timeout) {
        _ shouldBe empty
      }
    }

    "return empty seq when response status is not 200 OK" in {
      val service = addressServiceMock(responsePostcode(NOT_FOUND))

      val result = service.fetchAddressesForPostcode(PostcodeValid, ClearTextClientSideSessionFactory.DefaultTrackingId)

      whenReady(result, timeout) {
        _ shouldBe empty
      }
    }

    "return empty seq when response throws" in {
      val addressLookupService = addressServiceMock(responseThrows)

      val result = addressLookupService.fetchAddressesForPostcode(PostcodeValid, ClearTextClientSideSessionFactory.DefaultTrackingId)

      whenReady(result, timeout) {
        _ shouldBe empty
      }
    }

    "return empty seq given invalid json" in {
      val inputAsJson = Json.obj("addresses" -> "INVALID")
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressesForPostcode(PostcodeValid, ClearTextClientSideSessionFactory.DefaultTrackingId)

      whenReady(result, timeout) {
        _ shouldBe empty
      }
    }
  }

  "fetchAddressForUprn" should {
    "return AddressViewModel when response status is 200 OK" in {
      val service = addressServiceMock(responseValidForUprnToAddress)

      val result = service.fetchAddressForUprn(traderUprnValid.toString, ClearTextClientSideSessionFactory.DefaultTrackingId)

      whenReady(result, timeout) {
        case Some(addressViewModel) =>
          addressViewModel.uprn.map(_.toString) should equal(Some(traderUprnValid.toString))
          addressViewModel.address === uprnToAddressResponseValid.addressViewModel.get.address
        case _ => fail("Should have returned Some(AddressViewModel)")
      }
    }

    "return None when response statuss not 200 OK" in {
      val service = addressServiceMock(responseUprn(NOT_FOUND, UprnToAddressResponse(addressViewModel = None)))

      val result = service.fetchAddressForUprn(traderUprnValid.toString, ClearTextClientSideSessionFactory.DefaultTrackingId)

      whenReady(result, timeout) {
        _ should equal(None)
      }
    }

    "return none when response status is 200 OK but results is empty" in {
      val service = addressServiceMock(responseUprn(OK, UprnToAddressResponse(addressViewModel = None)))

      val result = service.fetchAddressForUprn(traderUprnValid.toString, ClearTextClientSideSessionFactory.DefaultTrackingId)

      whenReady(result, timeout) {
        _ should equal(None)
      }
    }

    "return none when web service throws an exception" in {
      val addressLookupService = addressServiceMock(responseThrows)

      val result = addressLookupService.fetchAddressForUprn(traderUprnValid.toString, ClearTextClientSideSessionFactory.DefaultTrackingId)

      whenReady(result) {
        _ should equal(None)
      }
    }

    "return empty seq given invalid json" in {
      val inputAsJson = Json.obj("addressViewModel" -> "INVALID")
      val service = addressServiceMock(response(OK, inputAsJson))

      val result = service.fetchAddressForUprn(PostcodeValid, ClearTextClientSideSessionFactory.DefaultTrackingId)

      whenReady(result, timeout) {
        _ shouldBe empty
      }
    }
  }

  private def addressServiceMock(response: Future[Response]): AddressLookupService = {
    // Using the real address lookup service but passing in a fake web service that returns the responses we specify.
    new AddressLookupServiceImpl(
      new FakeAddressLookupWebServiceImpl(responseOfPostcodeWebService = response, responseOfUprnWebService = response)
    )
  }

  private def response(statusCode: Int, inputAsJson: JsValue): Future[Response] = Future {
    FakeResponse(status = statusCode, fakeJson = Some(inputAsJson))
  }

  private def responsePostcode(statusCode: Int,
                       input: PostcodeToAddressResponse = postcodeToAddressResponseValid): Future[Response] = {
    val inputAsJson = Json.toJson(input)
    response(statusCode, inputAsJson)
  }

  private def responseUprn(statusCode: Int,
                   input: UprnToAddressResponse = uprnToAddressResponseValid): Future[Response] = {
    val inputAsJson = Json.toJson(input)
    response(statusCode, inputAsJson)
  }

  private val responseThrows: Future[Response] = Future {
    throw new RuntimeException("This error is generated deliberately by a test")
  }
}