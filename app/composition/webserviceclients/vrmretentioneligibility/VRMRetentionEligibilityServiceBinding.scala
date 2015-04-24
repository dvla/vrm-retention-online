package composition.webserviceclients.vrmretentioneligibility

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityService
import webserviceclients.vrmretentioneligibility.VRMRetentionEligibilityServiceImpl

final class VRMRetentionEligibilityServiceBinding extends ScalaModule {

  def configure() = {
    bind[VRMRetentionEligibilityService].to[VRMRetentionEligibilityServiceImpl].asEagerSingleton()
  }
}
