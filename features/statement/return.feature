Feature: Use the return statement in Rizzly
  As a developer
  I want to return form functions early and/or return values from functions
  In order to abort a function and/or return a value


Scenario: a return statement returns a value
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op: response(x: R{0,100}):R{0,100};
    
    elementary
      op: response(x: R{0,100}):R{0,100}
        return x;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(42) = 42


Scenario: a return statement aborts a function
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      out : signal();
    
    elementary
      tick: slot()
        return;
        out();
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect no more events

