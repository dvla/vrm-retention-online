package constraints.common

import helpers.UnitSpec

final class RegistrationNumberUnitSpec extends UnitSpec {
   "output format" should {
     "transform AB12CDE correctly" in {
       val vrm = "AB12CDE"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("AB12 CDE")
     }
     "transform A1 correctly" in {
       val vrm = "A1"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("A 1     ")
     }
     "transform A12 correctly" in {
       val vrm = "A12"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("A 12    ")
     }
     "transform A123 correctly" in {
       val vrm = "A123"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("A 123   ")
     }
     "transform A1234 correctly" in {
       val vrm = "A1234"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("A 1234  ")
     }
     "transform AB1 correctly" in {
       val vrm = "AB1"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("AB 1    ")
     }
     "transform AB12 correctly" in {
       val vrm = "AB12"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("AB 12   ")
     }
     "transform AB123 correctly" in {
       val vrm = "AB123"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("AB 123  ")
     }
     "transform AB1234 correctly" in {
       val vrm = "AB1234"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("AB 1234 ")
     }
     "transform ABC1 correctly" in {
       val vrm = "ABC1"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("ABC 1   ")
     }
     "transform ABC12 correctly" in {
       val vrm = "ABC12"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("ABC 12  ")
     }
     "transform ABC123 correctly" in {
       val vrm = "ABC123"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("ABC 123 ")
     }
     "transform ABC1234 correctly" in {
       val vrm = "ABC1234"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("ABC 1234")
     }
     "transform ABC1D correctly" in {
       val vrm = "ABC1D"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("ABC 1D  ")
     }
     "transform ABC12D correctly" in {
       val vrm = "ABC12D"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("ABC 12D ")
     }
     "transform ABC123D correctly" in {
       val vrm = "ABC123D"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("ABC 123D")
     }
     "transform 1A correctly" in {
       val vrm = "1A"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("1 A     ")
     }
     "transform '0001  A' correctly" in {
       val vrm = "0001  A"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("1 A     ")
     }
     "transform 1AB correctly" in {
       val vrm = "1AB"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("1 AB    ")
     }
     "transform '0001 AB' correctly" in {
       val vrm = "0001 AB"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("1 AB    ")
     }
     "transform 1ABC correctly" in {
       val vrm = "1ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("1 ABC   ")
     }
     "transform '0001ABC' correctly" in {
       val vrm = "0001ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("1 ABC   ")
     }
     "transform 12A correctly" in {
       val vrm = "12A"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("12 A    ")
     }
     "transform '0012  A' correctly" in {
       val vrm = "0012  A"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("12 A    ")
     }
     "transform 12AB correctly" in {
       val vrm = "12AB"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("12 AB   ")
     }
     "transform '0012 AB' correctly" in {
       val vrm = "0012 AB"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("12 AB   ")
     }
     "transform 12ABC correctly" in {
       val vrm = "12ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("12 ABC  ")
     }
     "transform '0012ABC' correctly" in {
       val vrm = "0012ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("12 ABC  ")
     }
     "transform 123A correctly" in {
       val vrm = "123A"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("123 A   ")
     }
     "transform '0123  A' correctly" in {
       val vrm = "0123  A"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("123 A   ")
     }
     "transform 123AB correctly" in {
       val vrm = "123AB"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("123 AB  ")
     }
     "transform '0123 AB' correctly" in {
       val vrm = "0123 AB"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("123 AB  ")
     }
     "transform 123ABC correctly" in {
       val vrm = "123ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("123 ABC ")
     }
     "transform '0123ABC' correctly" in {
       val vrm = "0123ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("123 ABC ")
     }
     "transform 1234A correctly" in {
       val vrm = "1234A"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("1234 A  ")
     }
     "transform '1234  A' correctly" in {
       val vrm = "1234  A"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("1234 A  ")
     }
     "transform 1234AB correctly" in {
       val vrm = "1234AB"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("1234 AB ")
     }
     "transform '1234 AB' correctly" in {
       val vrm = "1234 AB"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("1234 AB ")
     }
     "transform Y9ABC correctly" in {
       val vrm = "Y9ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("Y9 ABC  ")
     }
     "transform 'Y  9ABC' correctly" in {
       val vrm = "Y  9ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("Y9 ABC  ")
     }
     "transform Y12ABC correctly" in {
       val vrm = "Y12ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("Y12 ABC ")
     }
     "transform 'Y 12ABC' correctly" in {
       val vrm = "Y 12ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("Y12 ABC ")
     }
     "transform Y123ABC correctly" in {
       val vrm = "Y123ABC"

       val result = RegistrationNumber.formatVrm(vrm)

       result should equal("Y123 ABC")
     }
   }
 }