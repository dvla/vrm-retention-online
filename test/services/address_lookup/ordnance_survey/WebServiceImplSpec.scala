package services.address_lookup.ordnance_survey

import common.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.NoCookieFlags
import helpers.{UnitSpec, WireMockFixture}
import org.scalatest.concurrent.PatienceConfiguration.Interval
import scala.concurrent.duration.DurationInt
import play.api.i18n.Lang
import services.HttpHeaders
import services.fakes.FakeAddressLookupService.{PostcodeValid, PostcodeValidWithSpace}
import utils.helpers.Config
import com.github.tomakehurst.wiremock.client.WireMock.{getRequestedFor, urlEqualTo, equalTo}

final class WebServiceImplSpec extends UnitSpec  with WireMockFixture {

  val trackingIdValue = "trackingIdValue"
  val interval = Interval(50.millis)

  implicit val noCookieFlags = new NoCookieFlags

  implicit val clientSideSessionFactory = injector.getInstance(classOf[ClientSideSessionFactory])
  val addressLookupService = new services.address_lookup.ordnance_survey.WebServiceImpl(new Config() {
    override val ordnanceSurveyMicroServiceUrl = s"http://localhost:$wireMockPort"
  })

  "postcodeWithNoSpaces" should {
    "return the same string if no spaces present" in {
      val result = addressLookupService.postcodeWithNoSpaces(PostcodeValid)

      result should equal(PostcodeValid)
    }

    "remove spaces when present" in {
      val result = addressLookupService.postcodeWithNoSpaces(PostcodeValidWithSpace)

      result should equal(PostcodeValid)
    }
  }

  "callPostcodeWebService" should {
    "send the trackingId to the PostcodeWebService" in {
      val postCode = "N193NN"

      val futureResult = addressLookupService.callPostcodeWebService(postCode, trackingIdValue)(lang = Lang("en"))

      whenReady(futureResult, timeout, interval) { result =>
        wireMock.verifyThat(1, getRequestedFor(
          urlEqualTo(s"/postcode-to-address?postcode=$postCode&languageCode=EN&tracking_id=$trackingIdValue")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingIdValue)))
      }
    }

    "send the trackingId to the callUprnWebService" in {
      val postCode = "N193NN"

      val futureResult = addressLookupService.callUprnWebService(postCode, trackingIdValue)(Lang("en"))

      whenReady(futureResult, timeout, interval) { result =>
        wireMock.verifyThat(1, getRequestedFor(
          urlEqualTo(s"/uprn-to-address?uprn=$postCode&languageCode=EN&tracking_id=$trackingIdValue")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingIdValue)))
      }
    }
  }
}