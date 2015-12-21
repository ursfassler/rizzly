Feature: A transition can have a body
  As a developer
  I want transitions to have a body
  In order to execute code when the transition is taken

#TODO test transition with body
#TODO test scope of transition body
#TODO test access to event arguments in body

Scenario: A transition does not need to have a body
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();

    hfsm(A)
      A : state;
      B : state;
      
      A to B by tick();
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: A transition can have a body
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tock: signal();

    hfsm(A)
      A : state;
      B : state;
      
      A to B by tick() do
        tock();
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: The transition body is executed when the transition is taken
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tock: signal();

    hfsm(A)
      A : state;
      B : state;
      
      A to B by tick() do
        tock();
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect an event tock()
  And I expect no more events

  When I send an event tick()
  Then I expect no more events


Scenario: The transition body can access items of the surrounding scope
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tock: signal();

    hfsm(A)
      A : state;
      B : state;
      
      a : R{0,100} = 0;
      
      A to B by tick() do
        a := 42;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: The transition body can not access items of the source state
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tock: signal();

    hfsm(A)
      A : state
        a : R{0,100} = 0;
      end
      B : state;
      
      A to B by tick() do
        a := 42;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:12:5: Error: Name not found: a"


Scenario: The transition body can not access items of the destination state
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tock: signal();

    hfsm(A)
      A : state;
      B : state
        b : R{0,100} = 0;
      end
      
      A to B by tick() do
        b := 42;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:12:5: Error: Name not found: b"

