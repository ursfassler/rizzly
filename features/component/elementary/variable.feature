Feature: Use variables in the elementary implementation
  As a developer
  I want to use variables inside a elementary implementation
  In order save a state between events


Scenario: Declare a variable inside the component
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    elementary
      a : Boolean = False;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


#TODO write boolean output as False and True instead 0 and 1
Scenario: The variable keeps the state between events
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tock: signal(x: Boolean);

    elementary
      a : Boolean = False;
      
      tick: slot()
        tock(a);
        a := not a;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect an event tock(0)
  And I expect no more events

  When I send an event tick()
  Then I expect an event tock(1)
  And I expect no more events

