package webserviceclients.vrmretentioneligibility

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse

case class VRMRetentionEligibilityResponse(currentVRM: String, replacementVRM: Option[String])

case class VRMRetentionEligibilityResponseDto(response: Option[MicroserviceResponse],
                                              vrmRetentionEligibilityResponse: VRMRetentionEligibilityResponse)

object VRMRetentionEligibilityResponse {

  implicit val JsonFormat = Json.format[VRMRetentionEligibilityResponse]
}

object VRMRetentionEligibilityResponseDto {

  implicit val JsonFormat = Json.format[VRMRetentionEligibilityResponseDto]
}