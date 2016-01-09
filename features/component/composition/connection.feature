Feature: Connect components in an composition
  As a developer
  I want to easily connect components in a composition
  In order to have a clean and easy desciption of my connections

#TODO add asynchronous connections
#TODO add/move run to completion tests

Scenario: Slots does not need an implementation
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
    composition
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: Signals does not need to be conencted
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tock: signal();
    composition
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: Connect the input to the output
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tock: signal();
    composition
      tick -> tock;
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect an event tock()
  And I expect no more events


Scenario: Connect an instantiated component
  Given we have a file "testee.rzy" with the content:
    """
    Dummy = Component
      tick: slot();
      tock: signal();
    composition
      tick -> tock;
    end
    
    Testee = Component
      tick: slot();
      tock: signal();
    composition
      dummy: Dummy;

      tick -> dummy.tick;
      dummy.tock -> tock;
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect an event tock()
  And I expect no more events


Scenario: Connect multiple instantiated components
  Given we have a file "testee.rzy" with the content:
    """
    Dummy = Component
      tick: slot();
      tock: signal();
    composition
      tick -> tock;
    end
    
    Testee = Component
      tick: slot();
      tock: signal();
    composition
      dummy1: Dummy;
      dummy2: Dummy;

      tick -> dummy1.tick;
      dummy1.tock -> dummy2.tick;
      dummy2.tock -> tock;
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect an event tock()
  And I expect no more events


Scenario: Can connect a slot to multiple signals
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tockA: signal();
      tockB: signal();
    composition
      tick -> tockA;
      tick -> tockB;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario Outline: For multiple receivers, the order of event delivery is the same as the order of the connections
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tockA: signal();
      tockB: signal();
    composition
      tick -> <first>;
      tick -> <second>;
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect an event <first>()
  And I expect an event <second>()
  And I expect no more events

  Examples:
    | first | second |
    | tockA |  tockB |
    | tockB |  tockA |


Scenario: Can connect a signal from multiple slots
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      click: slot();
      tock: signal();
    composition
      tick -> tock;
      click -> tock;
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect an event tock()
  And I expect no more events

  When I send an event click()
  Then I expect an event tock()
  And I expect no more events


Scenario: Can connect a response
  Given we have a file "testee.rzy" with the content:
    """
    Dummy = Component
      get: response():R{0,100};
    elementary
      get: response():R{0,100}
        return 42;
      end
    end
    
    Testee = Component
      get: response():R{0,100};
    composition
      dummy: Dummy;

      get -> dummy.get;
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42


Scenario: Responses need to be connected
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      get: response():Boolean;
    composition
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:2:8: Error: Interface get not connected"


Scenario: Responses can not have more than one destination
  Given we have a file "testee.rzy" with the content:
    """
    Dummy = Component
      get: response():R{0,100};
    elementary
      get: response():R{0,100}
        return 42;
      end
    end
    
    Testee = Component
      get: response():R{0,100};
    composition
      a: Dummy;
      b: Dummy;

      get -> a.get;
      get -> b.get;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:16:7: Error: query needs exactly one connection, got more"


Scenario: Can connect a query
  Given we have a file "dummy2.rzy" with the content:
    """
    Testee = Component
      ask: query():R{0,100};
      get: response():R{0,100};
    composition
      get -> ask;
    end

    Dummy1 = Component
      get: response():R{0,100};
    elementary
      get: response():R{0,100}
        return 42;
      end
    end
    
    Dummy2 = Component
      get: response():R{0,100};
    composition
      dummy: Dummy1;
      testee: Testee;

      get -> testee.get;
      testee.ask -> dummy.get;
    end

    """

  When I succesfully compile "dummy2.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42


Scenario: Queries need to be connected
  Given we have a file "dummy.rzy" with the content:
    """
    Testee = Component
      ask: query():R{0,100};
    composition
    end
    
    Dummy = Component
    composition
      testee: Testee;
    end

    """

  When I start rizzly with the file "dummy.rzy"

  Then I expect an error code
  And stderr should contain "dummy.rzy:8:3: Error: Interface testee.ask not connected"


#TODO fix
@fixme
Scenario: Queries can not have more than one destination
  Given we have a file "dummy2.rzy" with the content:
    """
    Testee = Component
      ask: query():R{0,100};
    composition
    end

    Dummy1 = Component
      get: response():R{0,100};
    elementary
      get: response():R{0,100}
        return 42;
      end
    end
    
    Dummy2 = Component
    composition
      dummyA: Dummy1;
      dummyB: Dummy1;
      testee: Testee;

      testee.ask -> dummyA.get;
      testee.ask -> dummyB.get;
    end

    """

  When I start rizzly with the file "dummy2.rzy"

  Then I expect an error code
  And stderr should contain "dummy2.rzy:21:7: Error: TODO some nice error message"

