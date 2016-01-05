Feature: assign a new value to a variable
  As a developer
  I want to assign a new value to a variable
  In order to implement algorithms


Scenario: assign a value
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      set: slot(x, y: R{0,100});
      out: signal(x, y: R{0,100});
    
    elementary
      set: slot(x, y: R{0,100})
        x := y;
        out(x, y);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event set(91, 76)
  Then I expect an event out(76, 76)
  And I expect no more events


#TODO implement
@fixme
Scenario: assign a tuple to multiple variables
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      set: slot(x, y: R{0,100});
      out: signal(x, y: R{0,100});
    
    elementary
      set: slot(x, y: R{0,100})
        x, y := (y, x);
        out(x, y);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event set(91, 76)
  Then I expect an event out(76, 91)
  And I expect no more events


#TODO implement
@fixme
Scenario: in a multi assignment, values can be ignored
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      set: slot(a, b: R{0,100});
      out: signal(x: R{0,100});
    
    elementary
      set: slot(a, b: R{0,100})
        c : R{0,10};
        c, _ := (a, b);
        out(c);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event set(91, 76)
  Then I expect an event out(91)
  And I expect no more events

