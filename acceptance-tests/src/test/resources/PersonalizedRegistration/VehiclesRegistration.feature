Feature: Vehicles Personalized Registration

  As a User I want to Personalized my vehicle
  so that I can use my vehicle with law

  @HappyPath
  Scenario Outline: Happy Path - Keeper Acting
    Given that I have started the PR Retention Service
    When I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that is eligible for retention
    And I indicate that the keeper is acting
    Then the confirm keeper details page is displayed
  Examples:
    | vehicle-registration-number | document-reference-number | postcode |
    | "ABC1"                      | "11111111111"             | "SA11AA" |
    | "A1"                        | "11111111111"             | "AA11AA" |
    | "S11"                       | "11111111111"             | "SA1" |
    | "S12"                       | "11111111111"             | "SA1 ***" |

  @HappyPath
  Scenario Outline: Invalid Data in Vehicle Registration Number, Doc Ref ID and postcode
    Given that I have started the PR Retention Service
    When I enter invalid data in the <vehicle-registration-number>, <document-reference-number> and <postcode> fields
    Then the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and postcode fields are displayed
  Examples:
    | vehicle-registration-number | document-reference-number | postcode  |
    | "1XCG456"                   | "abgdrt12345"             | "SA000AS" |

  @UnHappyPath
  Scenario Outline: Vehicle Not Found
    Given that I have started the PR Retention Service
    When I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> that does not match a valid vehicle record
    Then the vehicle not found page is displayed
  Examples:
    | vehicle-registration-number | document-reference-number | postcode |
    | "C1"                        | "11111111111"             | "SA11AA" |

  @UnHappyPath
  Scenario Outline:Brute Force Lockout
    Given that I have started the PR Retention Service
    When I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> that does not match a valid vehicle record three times in a row
    Then the brute force lock out page is displayed
  Examples:
    | vehicle-registration-number | document-reference-number | postcode |
    | "ST05YYC"                   | "11111111111"             | "SA11AA" |

  @UnHappyPath
  Scenario Outline: Direct to Paper Channel
    Given that I have started the PR Retention Service
    When I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that is not eligible for retention
    Then the direct to paper channel page is displayed
  Examples:
    | vehicle-registration-number | document-reference-number | postcode |
    | "D1"                        | "11111111111"             | "SA11AA" |

  @UnHappyPath
  Scenario Outline: Vehicle not Eligible
    Given that I have started the PR Retention Service
    When I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that is not eligible for retention
    Then the vehicle not eligible page is displayed
  Examples:
    | vehicle-registration-number | document-reference-number | postcode |
    | "E1"                        | "11111111111"             | "SA11AA" |

  @HappyPath
  Scenario Outline: Trader Acting(no details stored)
    Given that I have started the PR Retention Service
    When I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that is eligible for retention and I indicate that the keeper is not acting and I have not previously chosen to store my details
    Then the supply business details page is displayed
  Examples:
    | vehicle-registration-number | document-reference-number | postcode |
    | "ABC1"                      | "11111111111"             | "SA11AA" |

  @HappyPath
  Scenario Outline: Trader Acting (details stored)
    Given that I have started the PR Retention Service
    When I enter data in the <vehicle-registration-number>, <document-reference-number> and <postcode> for a vehicle that and I indicate that the keeper is not acting and I have previously chosen to store my details and the cookie is still fresh less than seven days old
    Then the confirm business details page is displayed
  Examples:
    | vehicle-registration-number | document-reference-number | postcode |
    | "ABC1"                      | "11111111111"             | "SA11AA" |
