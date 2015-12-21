Feature: Instantiate components in an composition
  As a developer
  I want to instantiate component in the composition
  In order to use the behaviour of them


Scenario: Instantiate a component
  Given we have a file "testee.rzy" with the content:
    """
    Dummy = Component
    composition
    end
    
    Testee = Component
    composition
      dummy: Dummy;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: Can instantiated the component after the connection
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
      tick -> dummy.tick;
      dummy.tock -> tock;

      dummy: Dummy;
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect an event tock()
  And I expect no more events


Scenario: First instantiated component is initialized first
  Given we have a file "testee.rzy" with the content:
    """
    Dummy = Component
      tock: signal();
    elementary
      entry
        tock();
      end
    end
    
    Testee = Component
      tockA: signal();
      tockB: signal();
    composition
      a: Dummy;
      b: Dummy;
      
      a.tock -> tockA;
      b.tock -> tockB;
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect an event tockA()
  And I expect an event tockB()
  And I expect no more events


Scenario: First instantiated component is deinitialized last
  Given we have a file "testee.rzy" with the content:
    """
    Dummy = Component
      tock: signal();
    elementary
      exit
        tock();
      end
    end
    
    Testee = Component
      tockA: signal();
      tockB: signal();
    composition
      a: Dummy;
      b: Dummy;
      
      a.tock -> tockA;
      b.tock -> tockB;
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it
  And I deinitialize it

  Then I expect an event tockB()
  And I expect an event tockA()
  And I expect no more events

