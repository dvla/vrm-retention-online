package webserviceclients.vrmretentioneligibility

import composition.TestConfig
import helpers.UnitSpec
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.{VssWebEndUserDto, VssWebHeaderDto}
import webserviceclients.fakes.DateServiceConstants.{DayValid, MonthValid, YearValid}
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.{ReplacementRegistrationNumberValid, TrackingIdValid}

final class VRMRetentionEligibilityRequestSpec extends UnitSpec {

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

  private def toJson = {
    val request = VRMRetentionEligibilityRequest(buildWebHeader(TrackingIdValid), currentVRM = ReplacementRegistrationNumberValid, transactionTimestamp = dateTime)
    Json.toJson(request)
  }

  private val config = new TestConfig().build

  private def buildWebHeader(trackingId: String): VssWebHeaderDto = {
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