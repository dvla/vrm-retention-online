package webserviceclients.vehicleandkeeperlookup

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, postRequestedFor, urlEqualTo}
import helpers.{UnitSpec, WireMockFixture, WithApplication}
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.HttpHeaders
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

  private val lookupService = new VehicleAndKeeperLookupWebServiceImpl(new Config() {
    override val vehicleAndKeeperLookupMicroServiceBaseUrl = s"http://localhost:$wireMockPort"
  })

  private final val trackingId = "track-id-test"

  private val dateTime = new DateTime(
    YearValid.toInt,
    MonthValid.toInt,
    DayValid.toInt,
    0,
    0)

  private val request = VehicleAndKeeperDetailsRequest(
    referenceNumber = "ref number",
    registrationNumber = "reg number",
    transactionTimestamp = dateTime
  )

  private implicit val vehicleAndKeeperDetailsFormat = Json.format[VehicleAndKeeperDetailsRequest]
}