Feature: Use a while loop in Rizzly
  As a developer
  I want to use a while loop
  In order to have more flexibility than a for loop


Scenario: it is possible that a while loop is never executed
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: Boolean);
      out: signal();
    
    elementary
      inp: slot(x: Boolean)
        while x do
          out();
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(False)
  Then I expect no more events


Scenario: a while loop is executed as long as the predicate is true
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(a, b: Boolean);
      out: signal();
    
    elementary
      inp: slot(a, b: Boolean)
        while not (a and b) do
          out();
          a := a or b;
          b := not b;
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(False, False)
  Then I expect an event out()
  And I expect an event out()
  And I expect an event out()
  And I expect no more events

