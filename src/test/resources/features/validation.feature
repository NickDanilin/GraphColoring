Feature: Input validation
  Как пользователь,
  Я заинтересован в том, чтобы система проверяла входные данные,
  Чтобы я мог получить уведомление об ошибках и исправить их до начала раскраски

  # Императивный стиль
  Background:
    Given I select input option

  Rule: Manual Input
    Background:
      Given I select manual input

    Scenario: Valid console input
      Given I enter "5" in console
      And I enter "2 5" in console
      And I enter "1 3" in console
      And I enter "2 4" in console
      And I enter "3 5" in console
      And I enter "4 1" in console
      When I am finishing entering data
      Then I should see no error message

    Scenario: Invalid console input
      Given I enter "5" in console
      And I enter "2 6" in console
      And I enter "1 3" in console
      And I enter "2 4" in console
      And I enter "3 5" in console
      And I enter "4 1" in console
      When I am finishing entering data
      Then I should see error message

  Rule: JSON input
    Background:
      Given I select JSON input

    Scenario Outline: Valid JSON input
      Given I enter <data> in console
      When I am finishing entering data
      Then I should see no error message
      Examples:
        | data                                                                                                |
        | "{\"adjacency_list\": {\"1\": [2, 5], \"2\": [1, 3], \"3\": [2, 4], \"4\": [3, 5], \"5\": [4, 1]}}" |
        | "{\"adjacency_list\": {\"1\": [2, 3], \"2\": [1, 4], \"3\":[1], \"4\":[2]}}"                          |

    Scenario Outline: Invalid JSON input
      Given I enter <data> in console
      When I am finishing entering data
      Then I should see error message
      Examples:
        | data                                                                                                |
        | "{\"adjacency_list\": {\"1\": [2, 6], \"2\": [1, 3], \"3\": [2, 4], \"4\": [3, 5], \"5\": [4, 1]}}" |
        | "{\"adjacency_list\": {\"1\": [2, 3], \"2\": [1, 4, \"3\":[1], \"4\":[2]}}"                          |
