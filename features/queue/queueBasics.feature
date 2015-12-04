Feature: Use queues to connect components in Rizzly
  As a developer
  I want to use queues to connect components
  In order to asynchronously deliver events


Scenario: a simple queue
  Given we have a file "simple.rzy" with the content:
    """
    Simple = Component
      tick : slot();
      tock : signal();

    composition
      tick >> tock;
    end

    """

  When I succesfully compile "simple.rzy" with rizzly
  And fully compile everything

  When I initialize it
  Then I expect no more events
  
  When I send an event tick()
  Then I expect 1 events in the queue
  And I expect no more events
  
  When I process one event
  Then I expect 0 events in the queue
  And I expect an event tock()
  And I expect no more events


Scenario: a simple queue to an sub-component
  Given we have a file "simple2.rzy" with the content:
    """
    Dummy = Component
      tock : signal();
      tick : slot();

    elementary
      tick : slot()
        tock();
      end
    end

    Simple2 = Component
      tick : slot();
      tock : signal();

    composition
      dummy : Dummy;
      
      tick >> dummy.tick;
      dummy.tock >> tock;
    end

    """

  When I succesfully compile "simple2.rzy" with rizzly
  And fully compile everything

  When I initialize it
  Then I expect no more events
  
  When I send an event tick()
  Then I expect 1 events in the queue
  And I expect no more events
  
  When I process one event
  Then I expect 1 events in the queue
  And I expect no more events
  
  When I process one event
  Then I expect 0 events in the queue
  And I expect an event tock()
  And I expect no more events


Scenario Outline: a simple queue with data
  Given we have a file "simple3.rzy" with the content:
    """
    Simple3 = Component
      inp : slot( x: R{0,100} );
      out : signal( x: R{0,100} );

    composition
      inp >> out;
    end

    """

  When I succesfully compile "simple3.rzy" with rizzly
  And fully compile everything

  When I initialize it
  Then I expect no more events
  
  When I send an event inp(<value>)
  Then I expect 1 events in the queue
  And I expect no more events
  
  When I process one event
  Then I expect 0 events in the queue
  And I expect an event out(<value>)
  And I expect no more events

    Examples:
      | value |
      |     0 |
      |    42 |
      |    57 |


