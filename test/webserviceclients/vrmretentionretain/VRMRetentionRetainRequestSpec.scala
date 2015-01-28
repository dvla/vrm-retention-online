package webserviceclients.vrmretentionretain

import composition.TestConfig2
import helpers.UnitSpec
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import webserviceclients.fakes.DateServiceConstants.{DayValid, MonthValid, YearValid}
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.ReplacementRegistrationNumberValid

final class VRMRetentionRetainRequestSpec extends UnitSpec {

  val config = new TestConfig2 build

  "format" should {
    "write json with currentVRM" in {
      toJson.toString() should include(ReplacementRegistrationNumberValid)
    }

    "write json with ISO formatted data" in {
      toJson.toString() should include("1970-11-25T00:00:00.000")
    }
  }

  private def dateTime = new DateTime(
    YearValid.toInt,
    MonthValid.toInt,
    DayValid.toInt,
    0,
    0)
  private def request = VRMRetentionRetainRequest(buildWebHeader("1234567890"), currentVRM = ReplacementRegistrationNumberValid, transactionTimestamp = dateTime)
  private def toJson = Json.toJson(request)
  private def buildWebHeader(trackingId: String): VssWebHeaderDto =
  {
    VssWebHeaderDto(transactionId = trackingId,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.serviceTypeCode,
      buildEndUser())
  }
  private def buildEndUser(): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = config.orgBusinessUnit, orgBusUnit = config.orgBusinessUnit)
  }
}
