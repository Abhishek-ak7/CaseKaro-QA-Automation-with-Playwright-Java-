Feature: CaseKaro Product Search and Filter

  Scenario: Search for iPhone 16 Pro cases and verify pricing
    Given I am on the CaseKaro website
    When I navigate to Mobile Covers section
    And I search for "Apple" brand
    And I select Apple brand from the results
    And I search for "iPhone 16 Pro"
    And I select iPhone 16 Pro from the results
    And I filter for in-stock items
    Then I should be able to extract products from multiple pages
    And products should be sorted by price