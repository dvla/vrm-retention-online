package composition.webserviceclients.vrmretentioneligibility

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.fakes.FakeResponse
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityResponse

object Helper {

  def createResponse(response: (Int, VRMRetentionEligibilityResponse)) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}
