package helpers.common

import helpers.webbrowser.TestGlobal
import play.api.test.FakeApplication

object ProgressBar {
  val fakeApplicationWithProgressBarFalse = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map("progressBar.enabled" -> "false"))

  val fakeApplicationWithProgressBarTrue = FakeApplication(
    withGlobal = Some(TestGlobal),
    additionalConfiguration = Map("progressBar.enabled" -> "true"))

  val progressStep =
    List("EmptyStep",
      "Step 1 of 6",
      "Step 2 of 6",
      "Step 3 of 6",
      "Step 4 of 6",
      "Step 5 of 6",
      "Step 6 of 6"
    )
}