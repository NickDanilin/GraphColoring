Feature: Recommendation
  Как пользователь,
  Я хочу, чтобы система рекомендовала мне оптимальный алгоритм,
  Чтобы я мог получить лучшие результаты окраски.

  Бизнес-идея: рекомендация алгоритма на основе покрашенного графа.

  Scenario: Large graph
    Given I have a large graph
    When I ask for recommendations
    Then I should see recommendations on using DSATUR for large graphs

  Scenario: Small graph
    Given I have a small graph
    When I ask for recommendations
    Then I should see recommendations on using RLF for small graphs

  Scenario: Large sparse graph
    Given I have a large sparse graph
    When I ask for recommendations
    Then I should see recommendations on using DSATUR for large graphs
    And I should see recommendations on using DSATUR for very sparse graphs

  Scenario: Small high density graph
    Given I have a small high density graph
    When I ask for recommendations
    Then I should see recommendations on using RLF for small graphs
    And I should see recommendations on using RLF for high density graphs