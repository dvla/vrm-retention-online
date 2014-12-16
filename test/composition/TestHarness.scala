package composition

import helpers.webbrowser.TestHarnessBase

//import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.TestHarnessBase

trait TestHarness extends TestHarnessBase with DisposeGlobalCreator
