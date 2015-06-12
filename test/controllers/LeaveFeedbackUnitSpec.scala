package controllers

import com.tzavellas.sse.guice.ScalaModule
import composition.WithApplication
import helpers.UnitSpec
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import utils.helpers.Config

class LeaveFeedbackUnitSpec extends UnitSpec {

  val testUrl = "http://test/survery/url"

  "present" should {
    "contain code in the page source to open the survey in a new tab when a survey url is configured" in new WithApplication {
      val result = leaveFeedbackWithMockConfig(mockSurveyConfig()).present(FakeRequest())
      val expectedContent = s"window.open('$testUrl', '_blank')"
      contentAsString(result) should include(expectedContent)
    }

    "not contain code in the page source to open the survey in a new tab when a survey url is configured" in new WithApplication {
      val result = leaveFeedbackWithMockConfig(mockSurveyConfig(surveyUrl = None)).present(FakeRequest())
      val expectedContent = s"window.open('$testUrl', '_blank')"
      contentAsString(result) should not include expectedContent
    }
  }

  def leaveFeedbackWithMockConfig(config: Config): LeaveFeedback =
    testInjector(new ScalaModule() {
      override def configure(): Unit = bind[Config].toInstance(config)
    }).getInstance(classOf[LeaveFeedback])

  def mockSurveyConfig(surveyUrl: Option[String] = Some(testUrl)): Config = {
    val config = mock[Config]
    when(config.assetsUrl).thenReturn(None)
    when(config.googleAnalyticsTrackingId).thenReturn(None)
    when(config.surveyUrl).thenReturn(surveyUrl)
    config
  }
}
