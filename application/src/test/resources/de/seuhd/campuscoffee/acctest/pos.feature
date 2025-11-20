Feature: Points of Sale Management
  This feature allows users to create and modify points of sale (POS).

  Scenario: Insert and retrieve two POS
    Given an empty POS list
    When I insert POS with the following elements
      | name                   | description                      | type            | campus    | street          | houseNumber  | postalCode | city       |
      | Schmelzpunkt           | Great waffles                    | CAFE            | ALTSTADT  | Hauptstraße     | 90           | 69117      | Heidelberg |
      | Bäcker Görtz           | Walking distance to lecture hall | BAKERY          | INF       | Berliner Str.   | 43           | 69120      | Heidelberg |
      | New Vending Machine    | Use only in case of emergencies  | VENDING_MACHINE | BERGHEIM  | Teststraße      | 99a          | 12345      | Other City |
    Then the POS list should contain the same elements in the same order

# TODO: Add new scenario "Update one of three existing POS"
Scenario: Update one of three existing POS
  Given the following POS exist:
    | name        | description      | type  | campus    | street        | houseNumber | postalCode | city        |
    | Cafe One    | First desc       | CAFE  | ALTSTADT  | Hauptstraße   | 1           | 69117      | Heidelberg  |
    | Cafe Two    | Second desc      | CAFE  | ALTSTADT  | Hauptstraße   | 2           | 69117      | Heidelberg  |
    | Cafe Three  | Third desc       | CAFE  | ALTSTADT  | Hauptstraße   | 3           | 69117      | Heidelberg  |

  When I update the POS with name "Cafe Two" to have description "Updated description"
  Then the POS with name "Cafe Two" should have description "Updated description"
