package webserviceclients.vrmretentionretain

import composition.TestConfig
import helpers.UnitSpec
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common
import common.webserviceclients.common.VssWebEndUserDto
import common.webserviceclients.common.VssWebHeaderDto
import common.webserviceclients.emailservice.EmailServiceSendRequest
import common.webserviceclients.emailservice.From
import webserviceclients.fakes.DateServiceConstants.{DayValid, MonthValid, YearValid}
import webserviceclients.fakes.VrmRetentionRetainWebServiceConstants.ReplacementRegistrationNumberValid
import webserviceclients.paymentsolve.PaymentSolveUpdateRequest

class VRMRetentionRetainRequestSpec extends UnitSpec {

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
    0
  )

  private val config = new TestConfig().build

  private def request = VRMRetentionRetainRequest(
    buildWebHeader("1234567890"),
    currentVRM = ReplacementRegistrationNumberValid,
    transactionTimestamp = dateTime,
    PaymentSolveUpdateRequest(
      "",
      "",
      "",
      isPrimaryUrl = false,
      List(EmailServiceSendRequest("", "", None, From("", ""), "", None, None))
    ),
    Seq.empty,
    Seq.empty
  )

  private def toJson = Json.toJson(request)

  private def buildWebHeader(trackingId: String): VssWebHeaderDto = {
    VssWebHeaderDto(
      transactionId = trackingId,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser()
    )
  }

  private def buildEndUser(): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = config.orgBusinessUnit, orgBusUnit = config.orgBusinessUnit)
  }
}