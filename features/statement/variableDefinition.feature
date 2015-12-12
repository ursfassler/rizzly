Feature: declare function variables in Rizzly
  As a developer
  I want to declare variables in functions
  In order to store values for further usage


#TODO make variables by default constant
#TODO use "var" keyword to make variable
#TODO use constants in functions
#TODO remove "const" keyword
#TODO introduce "auto" keyword for automatic type declaration


#TODO by default, values are constant
@fixme
Scenario: constant definition
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot();
    
    elementary
      inp: slot()
        a : R{0,100};
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:6:5: Error: constant needs initialization"


Scenario: variable definition
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
      out: signal(x: R{0,100});
    
    elementary
      inp: slot(x: R{0,100})
        a : R{0,100};
        a := x;
        out(a);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(11)
  Then I expect an event out(11)
  And I expect no more events


Scenario: definition of 2 variables on the same line
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
      out: signal(x: R{0,100});
    
    elementary
      inp: slot(x: R{0,100})
        a, b : R{0,100};
        a := x;
        b := a;
        out(b);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(11)
  Then I expect an event out(11)
  And I expect no more events


Scenario: variable definition and initialization
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
      out: signal(x: R{0,100});
    
    elementary
      inp: slot(x: R{0,100})
        a : R{0,100} = x;
        out(a);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(11)
  Then I expect an event out(11)
  And I expect no more events


#TODO this should work
@fixme
Scenario: definition of 2 variables and initialization on the same line
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      set: slot(x, y: R{0,100});
      out: signal(x, y: R{0,100});
    
    elementary
      set: slot(x, y: R{0,100})
        a, b : R{0,100} = (x, y);
        out(a, b);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event set(91, 76)
  Then I expect an event out(91, 76)
  And I expect no more events

