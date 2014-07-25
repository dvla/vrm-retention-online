package constraints.common

import helpers.UnitSpec


final class RegistrationNumberUnitSpec extends UnitSpec {
   "output format" should {
     "transform AA88AAA correctly" in {
       val vrm = "AA88AAA"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("AA88 AAA")
     }
     "transform A9 correctly" in {
       val vrm = "A9"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("A 9     ")
     }
     "transform A99 correctly" in {
       val vrm = "A99"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("A 99    ")
     }
     "transform A999 correctly" in {
       val vrm = "A999"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("A 999   ")
     }
     "transform A9999 correctly" in {
       val vrm = "A9999"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("A 9999  ")
     }
   }
 }