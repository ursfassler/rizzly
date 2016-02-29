Feature: Use hierarchical finite state machines in  Rizzly
  As a developer
  I want to use hfsm
  In order to handle events easily


Scenario: states can have the same name as states with a different parent
  Given we have a file "stateNames1.rzy" with the content:
    """
    StateNames1 = Component

    hfsm(A)
      A : state(A)
        A : state(A)
          A : state;
          B : state;
          C : state;
        end
        B : state(A)
          A : state;
          B : state;
          C : state;
        end
      end
    end

    """

  When I start rizzly with the file "stateNames1.rzy"

  Then I expect no error


@fixme
Scenario: Unreachable states are detected
  Given we have a file "unusedState.rzy" with the content:
    """
    UnusedState = Component
      tick : slot();

    hfsm(A)
      A : state;
      B : state;
      C : state;

      A to B by tick();
      B to A by tick();
    end

    """

  When I start rizzly with the file "unusedState.rzy"

  Then I expect no error
  And stdout should contain "unusedState.rzy:7:3: Warning: Unused state: C"


Scenario: a state can have a variable
  Given we have a file "stateVariable2.rzy" with the content:
    """
    StateVariable2 = Component

    hfsm(A)
      A : state
        a : R{0,255} = 0;
      end
    end

    """

  When I start rizzly with the file "stateVariable2.rzy"

  Then I expect no error


