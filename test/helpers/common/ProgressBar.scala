package helpers.common

object ProgressBar {

  def progressStep(currentStep: Int): String = {
    val end = 6
    s"Step $currentStep of $end"
  }

  final val div: String = """<div class="progress-indicator">"""
}