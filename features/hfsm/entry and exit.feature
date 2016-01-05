Feature: entry and exit functions
  As a developer
  I want to execute code when enter or exit a state
  In order to action state depending listener

#TODO implement down propagation of transition:
@fixme
Scenario: a state exit function is not called when the state is not left
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick : slot();
      tock : signal();

    hfsm(A)
      A : state(A)
        exit
          tock();
        end

        A : state;
        B : state;
      end

      A to A.B by tick();
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it
  Then I expect no more events
  
  When I send an event tick()
  Then I expect no more events

