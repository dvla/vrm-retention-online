package webserviceclients.vrmretentioneligibility

import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId

import scala.concurrent.Future

trait VRMRetentionEligibilityService {

  def invoke(cmd: VRMRetentionEligibilityRequest,
             trackingId: TrackingId): Future[VRMRetentionEligibilityResponse]
}