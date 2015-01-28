package controllers

import com.google.inject.Inject
import uk.gov.dvla.vehicles.presentation.common.controllers
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.config.{OrdnanceSurveyConfig, VehicleAndKeeperLookupConfig}
import utils.helpers.{Config, Config2}

class Version @Inject()(vehiclesKeeperConfig: VehicleAndKeeperLookupConfig,
                        osAddressLookupConfig: OrdnanceSurveyConfig,
                        config: Config,
                        config2: Config2)
  extends controllers.Version(
    osAddressLookupConfig.baseUrl + "/version",
    vehiclesKeeperConfig.vehicleAndKeeperLookupMicroServiceBaseUrl + "/version",
    //    config.paymentSolveMicroServiceUrlBase + "/version",
    config2.vrmRetentionEligibilityMicroServiceUrlBase + "/version",
    config2.vrmRetentionRetainMicroServiceUrlBase + "/version"
  )
