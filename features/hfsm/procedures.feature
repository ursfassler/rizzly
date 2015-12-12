Feature: Use procedures int states
  As a developer
  I want to use procedures in states
  In order to cleanly organize my code

#TODO add more tests

Scenario: declare procedure on top level
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component

    hfsm(A)
      A : state;

      proc = procedure()
      end
    end

    """

  When I start rizzly with the file "testee.rzy"
  
  Then I expect no error

