Feature: Coloring graph
  Как пользователь,
  Я хочу чтобы система окрашивала графы
  Чтобы я мог использовать их для своих целей

  Scenario Outline: Color graph
    Given I have selected <algorithm> algorithm
    And I have adjacency list
      | 2 4 |
      | 1 3 |
      | 2 4 |
      | 1 3 |
    When I get coloring result
    Then Coloring is correct
    And Graph has 2 distinct colors
    Examples:
      | algorithm |
      | "DSatur"  |
      | "RLF"     |


  Scenario Outline: Load testing unit comparison
    Given I have selected <algorithm> algorithm
    And I get adjacency list from file <file>
    When I get coloring result
    Then Coloring is correct
    Examples:
      | algorithm | file                              |
      | "DSatur"  | "/datasets/unitgherkincompar.txt" |
      | "RLF"     | "/datasets/unitgherkincompar.txt" |


  Scenario Outline: Load testing
    Given I have selected <algorithm> algorithm
    And I get adjacency list from file <file>
    When I get coloring result
    Then Coloring is correct
    Examples:
      | algorithm | file                              |
      | "DSatur"  | "/datasets/graph_n10_p0.20.txt"   |
      | "RLF"     | "/datasets/graph_n10_p0.20.txt"   |
      | "DSatur"  | "/datasets/graph_n10_p0.50.txt"   |
      | "RLF"     | "/datasets/graph_n10_p0.50.txt"   |
      | "DSatur"  | "/datasets/graph_n10_p0.80.txt"   |
      | "RLF"     | "/datasets/graph_n10_p0.80.txt"   |
      | "DSatur"  | "/datasets/graph_n50_p0.20.txt"   |
      | "RLF"     | "/datasets/graph_n50_p0.20.txt"   |
      | "DSatur"  | "/datasets/graph_n50_p0.50.txt"   |
      | "RLF"     | "/datasets/graph_n50_p0.50.txt"   |
      | "DSatur"  | "/datasets/graph_n50_p0.80.txt"   |
      | "RLF"     | "/datasets/graph_n50_p0.80.txt"   |
      | "DSatur"  | "/datasets/graph_n200_p0.20.txt"  |
      | "RLF"     | "/datasets/graph_n200_p0.20.txt"  |
      | "DSatur"  | "/datasets/graph_n200_p0.50.txt"  |
      | "RLF"     | "/datasets/graph_n200_p0.50.txt"  |
      | "DSatur"  | "/datasets/graph_n200_p0.80.txt"  |
      | "RLF"     | "/datasets/graph_n200_p0.80.txt"  |
      | "DSatur"  | "/datasets/graph_n1000_p0.20.txt" |
      | "RLF"     | "/datasets/graph_n1000_p0.20.txt" |
      | "DSatur"  | "/datasets/graph_n1000_p0.50.txt" |
      | "RLF"     | "/datasets/graph_n1000_p0.50.txt" |
      | "DSatur"  | "/datasets/graph_n1000_p0.80.txt" |
      | "RLF"     | "/datasets/graph_n1000_p0.80.txt" |
      | "DSatur"  | "/datasets/graph_n2000_p0.20.txt" |
      | "RLF"     | "/datasets/graph_n2000_p0.20.txt" |
      | "DSatur"  | "/datasets/graph_n2000_p0.50.txt" |
      | "RLF"     | "/datasets/graph_n2000_p0.50.txt" |
      | "DSatur"  | "/datasets/graph_n2000_p0.80.txt" |
      | "RLF"     | "/datasets/graph_n2000_p0.80.txt" |
      | "DSatur"  | "/datasets/graph_n3000_p0.20.txt" |
      | "RLF"     | "/datasets/graph_n3000_p0.20.txt" |
      | "DSatur"  | "/datasets/graph_n3000_p0.50.txt" |
      | "RLF"     | "/datasets/graph_n3000_p0.50.txt" |
      | "DSatur"  | "/datasets/graph_n3000_p0.80.txt" |
      | "RLF"     | "/datasets/graph_n3000_p0.80.txt" |
      | "DSatur"  | "/datasets/graph_n5000_p0.20.txt" |
      | "RLF"     | "/datasets/graph_n5000_p0.20.txt" |
      | "DSatur"  | "/datasets/graph_n5000_p0.50.txt" |
      | "RLF"     | "/datasets/graph_n5000_p0.50.txt" |
      | "DSatur"  | "/datasets/graph_n5000_p0.80.txt" |
      | "RLF"     | "/datasets/graph_n5000_p0.80.txt" |
