Feature: Vehicles Personalized Registration

  As a User
  I want to Personalized my vehicle
  so that I can use my vehicle with law

  Background:
    Given that I have started the PR Retention Service

  @HappyPath
  Scenario Outline: Happy Path - Keeper Acting
    When I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that is eligible for retention
    And I indicate that the keeper is acting
    Then the confirm keeper details page is displayed
  Examples:
    | vehicle-registration-number | document-reference-number | postcode |
    | "ABC1"                      | "11111111111"             | "SA11AA" |
    | "A1"                        | "11111111111"             | "AA11AA" |
    | "S11"                       | "11111111111"             | "SA1" |
    | "S13"                       | "11111111111"             | "" |
    | "S101"                      | "11111111111"             | "SA222AA" |
    | "S102"                      | "11111111111"             | "SA222AA" |

  @UnHappyPath
  Scenario: Invalid Data in Vehicle Registration Number, Doc Ref ID and postcode
    When I enter invalid data in the "1XCG456", "abgdrt12345" and "SA000AS" fields
    Then the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and postcode fields are displayed

  @UnHappyPath
  Scenario: Vehicle Not Found
    # Don't change VNF1 as it is special cased in the mock brute force service not to lock the VRM.
    When I enter data in the "VNF1", "11111111111" and "SA11AA" that does not match a valid vehicle record
    Then the vrm not found page is displayed

  @UnHappyPath
  Scenario: Doc Ref Mismatch
    When I enter data in the "F1", "22222222222" and "AA11AA" that does not match a valid vehicle record
    Then the doc ref mismatch page is displayed
    And reset the "F1" so it won't be locked next time we run the tests

  @UnHappyPath
  Scenario: Brute Force Lockout
    When I enter data that does not match a valid vehicle record three times in a row
    Then the brute force lock out page is displayed

  @UnHappyPath
  Scenario: Direct to Paper Channel
    When I enter data in the "D1", "11111111111" and "SA11AA" for a vehicle that is not eligible for retention
    Then the direct to paper channel page is displayed
    And reset the "D1" so it won't be locked next time we run the tests

  @UnHappyPath
  Scenario: Vehicle not Eligible
    When I enter data in the "E1", "11111111111" and "SA11AA" for a vehicle that is not eligible for retention
    Then the vehicle not eligible page is displayed
    And reset the "E1" so it won't be locked next time we run the tests

  @HappyPath
  Scenario: Trader Acting(no details stored)
    When I enter data in the "ABC1", "11111111111" and "SA11AA" for a vehicle that is eligible for retention and I indicate that the keeper is not acting and I have not previously chosen to store my details
    Then the supply business details page is displayed

  @HappyPath
  Scenario: Trader Acting (details stored)
    When I enter data in the "ABC1", "11111111111" and "SA11AA" for a vehicle that and I indicate that the keeper is not acting and I have previously chosen to store my details and the cookie is still fresh less than seven days old
    Then the confirm business details page is displayed
