package webserviceclients.vrmretentionretain

import play.api.libs.json.Json
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.MicroserviceResponse

case class VRMRetentionRetainResponse(certificateNumber: Option[String],
                                      currentVRM: String, // pr mark
                                      replacementVRM: Option[String])

case class VRMRetentionRetainResponseDto(response: Option[MicroserviceResponse],
                                         vrmRetentionRetainResponse: VRMRetentionRetainResponse)

object VRMRetentionRetainResponse {

  implicit val JsonFormat = Json.format[VRMRetentionRetainResponse]
}

object VRMRetentionRetainResponseDto {

  implicit val JsonFormat = Json.format[VRMRetentionRetainResponseDto]
}
