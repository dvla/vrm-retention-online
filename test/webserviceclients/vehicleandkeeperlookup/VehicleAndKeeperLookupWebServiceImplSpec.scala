package webserviceclients.vehicleandkeeperlookup

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import helpers.WithApplication
import helpers.UnitSpec
import helpers.WireMockFixture
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.webserviceclients.common.DmsWebHeaderDto
import common.webserviceclients.HttpHeaders
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupConfig
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupRequest
import common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupWebServiceImpl
import common.clientsidesession.TrackingId
import webserviceclients.fakes.DateServiceConstants.{DayValid, MonthValid, YearValid}

class VehicleAndKeeperLookupWebServiceImplSpec extends UnitSpec with WireMockFixture {

  "callVehicleAndKeeperLookupService" should {

    "send the serialised json request" in new WithApplication {
      val resultFuture = lookupService.invoke(request, TrackingId(trackingId))
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/vehicleandkeeper/lookup/v1")
        ).withHeader(HttpHeaders.TrackingId, equalTo(trackingId)))
      }
    }
  }

  private def lookupService = new VehicleAndKeeperLookupWebServiceImpl(
    config = new VehicleAndKeeperLookupConfig() {
      override lazy val vehicleAndKeeperLookupMicroServiceBaseUrl = s"http://localhost:$wireMockPort"
    }
  )

  private final val trackingId = "track-id-test"

  private def dateTime = new DateTime(
    YearValid.toInt,
    MonthValid.toInt,
    DayValid.toInt,
    0,
    0
  )

  private def request = VehicleAndKeeperLookupRequest(
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
      serviceTypeCode = "test-vssServiceTypeCode",
      languageCode = englishLanguage,
      endUser = None)
  }

  private implicit val vehicleAndKeeperDetailsFormat = Json.format[VehicleAndKeeperLookupRequest]
}