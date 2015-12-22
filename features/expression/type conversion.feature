Feature: Use type conversion
  As a developer
  I want to use type conversion
  In order to be able to assign expressions of bigger types to variables


Scenario: I can assign a variable another one with a smaller type when I use type conversion
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,30});
      out: signal(x: R{10,20});

    elementary
      inp: slot(x: R{0,30})
        out(R{10,20}(x));
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario Outline: The type conversion does nothing when the value is in the range
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,30});
      out: signal(x: R{10,20});

    elementary
      inp: slot(x: R{0,30})
        out(R{10,20}(x));
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(<value>)
  Then I expect an event out(<value>)
  And I expect no more events
  
  Examples:
    | value |
    |    10 |
    |    15 |
    |    20 |


#TODO throw error
@fixme
Scenario Outline: The type conversion throws an error during run time when the value is out of range
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,30});
      out: signal(x: R{10,20});

    elementary
      inp: slot(x: R{0,30})
        out(R{10,20}(x));
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(<value>)
  Then I expect an event trap()
  And I expect no more events

  Examples:
    | value |
    |     0 |
    |     9 |
    |    21 |
    |    30 |


#TODO remove component
Scenario: The type conversion throws an error during compile time for compile time evaluated expressions
  Given we have a file "testee.rzy" with the content:
    """
    TheNumber : const = R{0,10}(20);
    
    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:1:22: Error: Value not in range: 20 not in 0..10"

