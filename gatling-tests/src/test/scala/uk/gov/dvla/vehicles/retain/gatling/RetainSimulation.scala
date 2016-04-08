package uk.gov.dvla.vehicles.retain.gatling

import io.gatling.core.Predef._
import Helper.httpConf
import Scenarios._

class RetainSimulation extends Simulation {

  private val oneUser = atOnceUsers(1)

  setUp(
    // Happy paths
    assetsAreAccessible.inject(oneUser),

    // KEEP COMMENTED OUT CODE BELOW AS IT MAY BE USEFUL FOR AUDIT
//    registeredKeeperAndFullKeeperAddress.inject(oneUser),
//    registeredKeeperAndMakeNoModel.inject(oneUser),
//    registeredKeeperAndModelNoMake.inject(oneUser),
//    registeredKeeperAndPartialKeeperAddress.inject(oneUser),
    registeredKeeperVeryLongMakeAndModel.inject(oneUser),

    // The following test is currently failing due to a mod_security issue
    // https://github.com/SpiderLabs/ModSecurity/issues/582
    // TODO uncommented when the above mod security problem is addressed.
    // notRegisteredKeeperAndFullKeeperAddress.inject(oneUser),

    // Sad paths
    eligibilityCheckDirectToPaper.inject(oneUser),

    notEligibleToTransact.inject(oneUser),
    vrmNotFound.inject(oneUser)
  ).
    protocols(httpConf).
    assertions(global.failedRequests.count.is(0))
}
