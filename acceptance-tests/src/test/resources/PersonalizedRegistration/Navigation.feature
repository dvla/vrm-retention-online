Feature: Navigation

  Background:
    Given that I have started the PR Retention Service

  Scenario Outline: Entering a url that is before the origin page (keeper acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin                                        | target                        | expected                      | filled   |
    | "confirm"                                     | "vehicle-lookup"              | "vehicle-lookup"              | "filled" |
    | "payment (keeper acting)"                     | "vehicle-lookup"              | "vehicle-lookup"              | "filled" |
    | "payment (keeper acting)"                     | "confirm"                     | "confirm"                     | "-" |

  Scenario Outline: Entering a url that is after the origin page
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin                                        | target                        | expected                      | filled       |
    | "vehicle-lookup"                              | "confirm"                     | "vehicle-lookup"              | "not filled" |
    | "vehicle-lookup"                              | "payment"                     | "vehicle-lookup"              | "not filled" |
    | "vehicle-lookup"                              | "success"                     | "vehicle-lookup"              | "not filled" |
    | "confirm"                                     | "payment"                     | "confirm"                     | "not filled" |
    | "confirm"                                     | "success"                     | "confirm"                     | "not filled" |
    # An odd case, we won't have a usable referrer in the header so cannot go back to the payment page.
    | "payment (keeper acting)"                     | "success"                     | "confirm"                     | "not filled"     |

  Scenario Outline: Entering a url that is before the origin page (business acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin | target | expected | filled |
    | "setup-business-details"                                                   | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "business-choose-your-address"                                             | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "business-choose-your-address"                                             | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "enter-address-manually"                                                   | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "enter-address-manually"                                                   | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "enter-address-manually"                                                   | "business-choose-your-address" | "business-choose-your-address" | "not filled" |
    | "confirm-business"                                                         | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "confirm-business"                                                         | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "confirm-business"                                                         | "business-choose-your-address" | "business-choose-your-address" | "filled"     |
    | "confirm-business"                                                         | "enter-address-manually"       | "enter-address-manually"       | "not filled" |
    | "confirm-business (entered address manually)"                              | "enter-address-manually"       | "enter-address-manually"       | "filled"     |
    | "confirm (business acting)"                                                | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "confirm (business acting)"                                                | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "confirm (business acting)"                                                | "business-choose-your-address" | "business-choose-your-address" | "filled"     |
    | "confirm (business acting) (entered address manually)"                     | "enter-address-manually"       | "enter-address-manually"       | "filled"     |
    | "confirm (business acting)"                                                | "confirm-business"             | "confirm-business"             | "filled"     |
    | "payment (business acting)"                                                | "vehicle-lookup"               | "vehicle-lookup"               | "filled"     |
    | "payment (business acting)"                                                | "setup-business-details"       | "setup-business-details"       | "filled"     |
    | "payment (business acting)"                                                | "business-choose-your-address" | "business-choose-your-address" | "filled"     |
    | "payment (business acting)"                                                | "enter-address-manually"       | "enter-address-manually"       | "not filled" |
    | "payment (business acting) (entered address manually)"                     | "enter-address-manually"       | "enter-address-manually"       | "filled"     |
    | "payment (business acting)"                                                | "confirm-business"             | "confirm-business"             | "filled"     |
    | "payment (business acting)"                                                | "confirm"    | "confirm"    | "not filled"     |

  Scenario Outline: Pressing the browser's back button back to the start page
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the payment, retain and both vehicle-and-keeper cookies are <wiped>
  Examples:
    | origin           | expected           | wiped   |
    | "vehicle-lookup" | "before-you-start" | "wiped" |

  Scenario Outline: Pressing the browser's back button (keeper acting)
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin                    | expected         | filled       |
    | "confirm"                 | "vehicle-lookup" | "filled"     |
    | "payment (keeper acting)" | "confirm"        | "not filled" |

  Scenario Outline: Entering a url that is after the origin page (business acting)
    Given that I am on the <origin> page
    When I enter the url for the <target> page
    Then I am redirected to the <expected> page
    And the <expected> form is not filled with the values I previously entered
  Examples:
    | origin                         | target                         | expected                       |
    | "setup-business-details"       | "business-choose-your-address" | "setup-business-details"       |
    | "setup-business-details"       | "enter-address-manually"       | "setup-business-details"       |
    | "setup-business-details"       | "confirm-business"             | "setup-business-details"       |
    | "setup-business-details"       | "confirm"                      | "setup-business-details"       |
    | "setup-business-details"       | "payment"                      | "setup-business-details"       |
    | "setup-business-details"       | "success"                      | "setup-business-details"       |
    | "business-choose-your-address" | "enter-address-manually"       | "enter-address-manually"       |
    | "business-choose-your-address" | "confirm-business"             | "business-choose-your-address" |
    | "business-choose-your-address" | "confirm"                      | "business-choose-your-address" |
    | "business-choose-your-address" | "payment"                      | "business-choose-your-address" |
    | "business-choose-your-address" | "success"                      | "business-choose-your-address" |
    | "enter-address-manually"       | "confirm-business"             | "business-choose-your-address" |
    | "enter-address-manually"       | "confirm"                      | "business-choose-your-address" |
    | "enter-address-manually"       | "payment"                      | "business-choose-your-address" |
    | "enter-address-manually"       | "success"                      | "business-choose-your-address" |
    | "confirm-business"             | "confirm"                      | "confirm-business"             |
    | "confirm-business"             | "payment"                      | "confirm-business"             |
    | "confirm-business"             | "success"                      | "confirm-business"             |

  Scenario Outline: Pressing the browser's back button (business acting)
    Given that I am on the <origin> page
    When I press the browser's back button
    Then I am redirected to the <expected> page
    And the <expected> form is <filled> with the values I previously entered
  Examples:
    | origin                                          | expected                       | filled       |
    | "setup-business-details"                        | "vehicle-lookup"               | "filled"     |
    | "business-choose-your-address"                  | "setup-business-details"       | "filled"     |
    | "enter-address-manually"                        | "business-choose-your-address" | "not filled" |
    | "confirm-business (entered address manually)"   | "enter-address-manually"       | "filled"     |
    | "confirm-business"                              | "business-choose-your-address" | "filled"     |