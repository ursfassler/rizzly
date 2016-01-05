Feature: Transition source and destination
  As a developer
  I want to specify the source and destination for transitions flexible
  In order to handle all use cases


Scenario: A transition can come and go to states on the same level
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick : slot();

    hfsm(A)
      A : state;
      B : state;

      A to B by tick();
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: A transition source state has to have a full path
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick : slot();

    hfsm(A)
      A : state(AA)
        AA: state;
      end
      B : state;

      AA to B by tick();
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:10:3: Fatal: Item not found"


Scenario: A transition source state has to have a full path
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick : slot();

    hfsm(A)
      A : state(AA)
        AA: state;
      end
      B : state;

      B to AA by tick();
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:10:8: Fatal: Item not found"


Scenario: A transition can come and go to sub-states
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick : slot();

    hfsm(A)
      A : state(AA)
        AA: state;
      end
      B : state(BA)
        BA: state;
      end

      A.AA to B.BA by tick();
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: A transition can not go to an outer state
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick : slot();

    hfsm(A)
      A : state(B)
        B : state;
        
        B to A by tick();
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:8:10: Fatal: Item not found"


Scenario: A transition can not come from an outer state
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick : slot();

    hfsm(A)
      A : state(B)
        B : state;
        
        A to B by tick();
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:8:5: Fatal: Item not found"


Scenario: A transition can go to the enclosing state
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tock: signal();

    hfsm(A)
      A : state
        entry
          tock();
        end
      end
      
      A to self by tick();
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything

  When I initialize it
  Then I expect an event tock()
  Then I expect no more events

  When I send an event tick()
  Then I expect an event tock()
  And I expect no more events


Scenario: A transition can come from the enclosing state
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tock: signal();

    hfsm(A)
      A : state;
      
      B : state
        entry
          tock();
        end
      end
      
      self to B by tick();
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything

  When I initialize it
  Then I expect no more events

  When I send an event tick()
  Then I expect an event tock()
  And I expect no more events


