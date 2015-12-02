Feature: Transition source and destination
  As a developer
  I want to specify the source and destination for transitions flexible
  In order to handle all use cases


Scenario: I can access states by fully specify their name
  Given we have a file "stateNames2.rzy" with the content:
    """
    StateNames2 = Component
      tick : slot();
      evt : signal(value: R{0,255});

    hfsm(A)
      A : state(A)
        A : state(A)
          A : state
            entry
              evt(1);
            end
          end
        end
        B : state(A)
          A : state
            entry
              evt(2);
            end
          end
        end
      end

      A.A to B.A by tick();
    end

    """

  When I succesfully compile "stateNames2.rzy" with rizzly
  And fully compile everything

  When I initialize it
  Then I expect an event evt(1)
  And I expect no more events

  When I send an event tick()
  Then I expect an event evt(2)
  And I expect no more events


Scenario: A transition can not go to an outer state
  Given we have a file "transDist1.rzy" with the content:
    """
    TransDist1 = Component
      tick : slot();

    hfsm(A)
      A : state(AA)
        AA : state
          AA to A by tick();
        end
      end
    end

    """

  When I start rizzly with the file "transDist1.rzy"

  Then I expect an error code
  And stderr should contain "transDist1.rzy:7:13: Error: State not found: A"


Scenario: A transition can not come from an outer state
  Given we have a file "transDist1.rzy" with the content:
    """
    TransDist1 = Component
      tick : slot();

    hfsm(A)
      A : state(AA)
        AA : state
          A to AA by tick();
        end
      end
    end

    """

  When I start rizzly with the file "transDist1.rzy"

  Then I expect an error code
  And stderr should contain "transDist1.rzy:7:7: Error: State not found: A"

