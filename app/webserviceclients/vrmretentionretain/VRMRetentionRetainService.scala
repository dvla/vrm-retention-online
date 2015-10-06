package webserviceclients.vrmretentionretain

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

import scala.concurrent.Future

trait VRMRetentionRetainService {

  def invoke(cmd: VRMRetentionRetainRequest, trackingId: TrackingId): Future[(Int, VRMRetentionRetainResponseDto)]
}