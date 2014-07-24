package constraints.common

import helpers.UnitSpec


final class PostcodeUnitSpec extends UnitSpec {
  "output format" should {
    "transform 'A12BC' correctly" in {
      val postcode = "A12BC"

      val result = Postcode.formatPostcode(postcode)

      result should equal("A1 2BC")
    }
    "transform 'A 012BC' correctly" in {
      val postcode = "A 012BC"

      val result = Postcode.formatPostcode(postcode)
      println(result)
      result should equal("A1 2BC")
    }
    "transform 'A123BC' correctly" in {
      val postcode = "A123BC"

      val result = Postcode.formatPostcode(postcode)

      result should equal("A12 3BC")
    }
    "transform 'A 123BC' correctly" in {
      val postcode = "A 123BC"

      val result = Postcode.formatPostcode(postcode)

      result should equal("A12 3BC")
    }
    "transform 'A1B2CD' correctly" in {
      val postcode = "A1B2CD"

      val result = Postcode.formatPostcode(postcode)

      result should equal("A1B 2CD")
    }
    "transform 'A 1B2CD' correctly" in {
      val postcode = "A 1B2CD"

      val result = Postcode.formatPostcode(postcode)

      result should equal("A1B 2CD")
    }
    "transform 'AB12CD' correctly" in {
      val postcode = "AB12CD"

      val result = Postcode.formatPostcode(postcode)

      result should equal("AB1 2CD")
    }
    "transform 'AB012CD' correctly" in {
      val postcode = "AB012CD"

      val result = Postcode.formatPostcode(postcode)

      result should equal("AB1 2CD")
    }
    "transform 'AB123CD' correctly" in {
      val postcode = "AB123CD"

      val result = Postcode.formatPostcode(postcode)

      result should equal("AB12 3CD")
    }
    "transform 'AB1C2DE' correctly" in {
      val postcode = "AB1C2DE"

      val result = Postcode.formatPostcode(postcode)

      result should equal("AB1C 2DE")
    }
  }
}