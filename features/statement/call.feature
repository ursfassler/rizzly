Feature: Call procedures in Rizzly
  As a developer
  I want to call procedures
  In order to organize my code


Scenario: a simple procedure call
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tack: signal();
    
    elementary
      tick: slot()
        doTack();
      end
      
      doTack = procedure()
        tack();
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect an event tack()
  And I expect no more events


Scenario: a procedure call with arguments
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      set: slot(a, b: R{0,100});
      tack: signal(a, b: R{0,100});
    
    elementary
      set: slot(a, b: R{0,100})
        doTack(a, b);
      end
      
      doTack = procedure(a, b: R{0,100})
        tack(a, b);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event set(42, 57)
  Then I expect an event tack(42, 57)
  And I expect no more events

