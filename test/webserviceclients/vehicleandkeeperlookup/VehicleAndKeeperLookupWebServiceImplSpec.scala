package webserviceclients.vehicleandkeeperlookup

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import composition.{TestConfig2, TestConfig, WithApplication}
import helpers.{UnitSpec, WireMockFixture}
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{DmsWebEndUserDto, DmsWebHeaderDto}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsRequest
import utils.helpers.Config
import webserviceclients.fakes.DateServiceConstants._

final class VehicleAndKeeperLookupWebServiceImplSpec extends UnitSpec with WireMockFixture {

  "callVehicleAndKeeperLookupService" should {

    "send the serialised json request" in new WithApplication {
      val resultFuture = lookupService.invoke(request, trackingId)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/vehicleandkeeper/lookup/v1")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId)))
      }
    }
  }

  private def lookupService = new VehicleAndKeeperLookupWebServiceImpl(
    config = new TestConfig(vehicleAndKeeperLookupMicroServiceBaseUrl = s"http://localhost:$wireMockPort").build,
    config2 = new TestConfig2(vehicleAndKeeperLookupMicroServiceBaseUrl = s"http://localhost:$wireMockPort").build
  )

  private final val trackingId = "track-id-test"

  private def dateTime = new DateTime(
    YearValid.toInt,
    MonthValid.toInt,
    DayValid.toInt,
    0,
    0)

  private def request = VehicleAndKeeperDetailsRequest(
    dmsHeader = buildHeader(trackingId),
    referenceNumber = "ref number",
    registrationNumber = "reg number",
    transactionTimestamp = dateTime
  )

  private def buildHeader(trackingId: String): DmsWebHeaderDto = {
    val alwaysLog = true
    val englishLanguage = "EN"
    DmsWebHeaderDto(conversationId = trackingId,
      originDateTime = dateTime,
      applicationCode = "test-applicationCode",
      channelCode = "test-channelCode",
      contactId = 42,
      eventFlag = alwaysLog,
      serviceTypeCode = "test-serviceTypeCode",
      languageCode = englishLanguage,
      endUser = None)
  }

  private implicit val vehicleAndKeeperDetailsFormat = Json.format[VehicleAndKeeperDetailsRequest]
}