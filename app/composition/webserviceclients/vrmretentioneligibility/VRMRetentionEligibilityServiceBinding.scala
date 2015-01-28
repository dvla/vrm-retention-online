package composition.webserviceclients.vrmretentioneligibility

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityServiceImpl, VRMRetentionEligibilityService}

final class VRMRetentionEligibilityServiceBinding extends ScalaModule {

  def configure() = {
    bind[VRMRetentionEligibilityService].to[VRMRetentionEligibilityServiceImpl].asEagerSingleton()
  }
}
