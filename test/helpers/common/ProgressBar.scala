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

  val progressStep: List[String] = {
    val start = 1
    val end = 6
    List.range(start, end).map(n => s"Step $n of $end")
  }
}