package composition

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityService, VRMRetentionEligibilityServiceImpl, VRMRetentionEligibilityWebService, VRMRetentionEligibilityWebServiceImpl}

final class VRMRetentionEligibilityBinding extends ScalaModule {

  def configure() = {
    bind[VRMRetentionEligibilityWebService].to[VRMRetentionEligibilityWebServiceImpl].asEagerSingleton()
    bind[VRMRetentionEligibilityService].to[VRMRetentionEligibilityServiceImpl].asEagerSingleton()
  }
}