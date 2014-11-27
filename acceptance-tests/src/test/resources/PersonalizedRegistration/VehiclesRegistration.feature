Feature: Vehicles Personalized Registration

  As a User I want to Personalized my vehicle
  so that I can use my vehicle with law

  @HappyPath
  Scenario Outline: Happy Path - Keeper Acting
    Given that I have started the PR Retention Service
    When I enter data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> for a vehicle that is eligible for retention
    And I indicate that the keeper is acting
    Then the confirm keeper details page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "ABC1"                    | "11111111111" | "SA11AA" |

  @HappyPath
  Scenario Outline: Invalid Data in Vehicle Registration Number, Doc Ref ID and Postcode
    Given that I have started the PR Retention Service
    When I enter invalid data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> fields
    Then the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode  |
    | "1XCG456"                 | "abgdrt12345" | "SA000AS" |

  @UnHappyPath
  Scenario Outline: Vehicle Not Found
    Given that I have started the PR Retention Service
    When I enter data in the <Vehicle-Registration-Number>, <Doc-Ref-ID> and <Postcode> that does not match a valid vehicle record
    Then the vehicle not found page is displayed
  Examples:
    | Vehicle-Registration-Number | Doc-Ref-ID    | Postcode |
    | "C1"                        | "11111111111" | "SA11AA" |

  @UnHappyPath
  Scenario Outline:Brute Force Lockout
    Given that I have started the PR Retention Service
    When I enter data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> that does not match a valid vehicle record three times in a row
    Then the brute force lock out page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "ST05YYC"                 | "11111111111" | "SA11AA" |

  @UnHappyPath
  Scenario Outline: Vehicle not Eligible
    Given that I have started the PR Retention Service
    When I enter data in the <VehicleRegistrationNumber>, <DocRefID> and <Postcode> for a vehicle that is not eligible for retention
    Then the vehicle not eligible page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "E1"                      | "11111111111" | "SA11AA" |