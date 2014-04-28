package controllers.disposal_of_vehicle

import play.api.test.{FakeRequest, WithApplication}
import play.api.test.Helpers._
import helpers.UnitSpec

class UprnNotFoundUnitSpec extends UnitSpec {

  "UprnNotFound - Controller" should {

    "present" in new WithApplication {
      val request = FakeRequest().withSession()
      val result = new UprnNotFound().present(request)
      whenReady(result) {
        r => r.header.status should equal(OK)
      }
    }
  }
}
