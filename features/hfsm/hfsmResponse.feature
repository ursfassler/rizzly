Feature: Response implementation in hfsm
  As a developer
  I want to return a different response depeding on the state i am in
  In order to easily respond state dependent information

Scenario: default responses
  Given we have a file "response.rzy" with the content:
    """
    Response = Component
      tick : slot();
      read : response():R{0,100};

    hfsm( A )
      A to B by tick();
      B to C by tick();
      C to A by tick();

      A : state;

      B : state
        read : response():R{0,100}
          return 42;
        end
      end

      C : state;

      read : response():R{0,100}
        return 23;
      end
    end

    """

  When I succesfully compile "response.rzy" with rizzly
  And fully compile everything

  When I initialize it
  Then I expect the request read() = 23

  When I send an event tick()
  Then I expect the request read() = 42

  When I send an event tick()
  Then I expect the request read() = 23


Scenario: use first matching response
  Given we have a file "response3.rzy" with the content:
    """
    Response3 = Component
      read : response():R{0,100};

    hfsm( A )
      read : response():R{0,100}
        return 57;
      end

      A : state
        read : response():R{0,100}     // this response is never used
          return 42;
        end
      end
    end

    """

  When I succesfully compile "response3.rzy" with rizzly
  And fully compile everything

  When I initialize it
  Then I expect the request read() = 57


Scenario: no default response is needed when all sub-states have the response defined
  Given we have a file "response4.rzy" with the content:
    """
    Response4 = Component
      tick : slot();
      read : response():R{0,100};

    hfsm( A )
      A to B by tick();

      A : state
        read : response():R{0,100}
          return 1;
        end
      end

      B : state
        read : response():R{0,100}
          return 2;
        end
      end
    end

    """

  When I succesfully compile "response4.rzy" with rizzly
  And fully compile everything

  When I initialize it
  Then I expect the request read() = 1

  When I send an event tick()
  Then I expect the request read() = 2


