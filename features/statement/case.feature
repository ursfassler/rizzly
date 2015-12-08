Feature: Use a case statement in Rizzly
  As a developer
  I want to use a case
  In order to have a multi branch


#TODO use lowercase for error message
@fixme
Scenario: a case statement can not have an else only
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
    
    elementary
      inp: slot(x: R{0,100})
        case x of
          else
          end
        end
      end
    end

    """

  When I start rizzly with the file "testee.rzy"
  
  Then I expect an error code
  And stderr should contain "testee.rzy:7:7: Fatal: Unexpected token: ELSE"


Scenario: a case statement can have multiple branches
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
      branch1: signal();
      branch2: signal();
    
    elementary
      inp: slot(x: R{0,100})
        case x of
          0:
            branch1();
          end
          2:
            branch2();
          end
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(0)
  Then I expect an event branch1()
  And I expect no more events

  When I send an event inp(1)
  Then I expect no more events

  When I send an event inp(2)
  Then I expect an event branch2()
  And I expect no more events


Scenario Outline: a case statement can have a branch and an else
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
      branch1: signal();
      branch2: signal();
    
    elementary
      inp: slot(x: R{0,100})
        case x of
          0:
            branch1();
          end
          else
            branch2();
          end
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(0)
  Then I expect an event branch1()
  And I expect no more events

  Examples:
    | value | event     |
    |     0 | branch1() |
    |     1 | branch2() |
    |     2 | branch2() |


Scenario Outline: a case can be over a range
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
      branch1: signal();
      branch2: signal();
      branch3: signal();
    
    elementary
      inp: slot(x: R{0,100})
        case x of
          0..1:
            branch1();
          end
          4..6:
            branch2();
          end
          else
            branch3();
          end
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(<value>)
  Then I expect an event <event>
  And I expect no more events

  Examples:
    | value | event     |
    |     0 | branch1() |
    |     1 | branch1() |
    |     2 | branch3() |
    |     3 | branch3() |
    |     4 | branch2() |
    |     5 | branch2() |
    |     6 | branch2() |
    |     7 | branch3() |


Scenario Outline: a case can be over multiple values
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
      branch1: signal();
      branch2: signal();
    
    elementary
      inp: slot(x: R{0,100})
        case x of
          0, 2, 5:
            branch1();
          end
          else
            branch2();
          end
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(<value>)
  Then I expect an event <event>
  And I expect no more events

  Examples:
    | value | event     |
    |     0 | branch1() |
    |     1 | branch2() |
    |     2 | branch1() |
    |     3 | branch2() |
    |     4 | branch2() |
    |     5 | branch1() |
    |     6 | branch2() |


Scenario Outline: a case can be over multiple values combined with ranges
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
      branch1: signal();
      branch2: signal();
    
    elementary
      inp: slot(x: R{0,100})
        case x of
          0, 2..4:
            branch1();
          end
          else
            branch2();
          end
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(<value>)
  Then I expect an event <event>
  And I expect no more events

  Examples:
    | value | event     |
    |     0 | branch1() |
    |     1 | branch2() |
    |     2 | branch1() |
    |     3 | branch1() |
    |     4 | branch1() |
    |     5 | branch2() |
    |     6 | branch2() |


@fixme
Scenario: a case value can not be used twice
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
    
    elementary
      inp: slot(x: R{0,100})
        case x of
          0:
          end
          0:
          end
        end
      end
    end

    """

  When I start rizzly with the file "testee.rzy"
  
  Then I expect an error code
  And stderr should contain "testee.rzy:9:7: Error:"


@fixme
Scenario: a case value can not be used when it is used in another branch
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
    
    elementary
      inp: slot(x: R{0,100})
        case x of
          0..2:
          end
          1:
          end
        end
      end
    end

    """

  When I start rizzly with the file "testee.rzy"
  
  Then I expect an error code
  And stderr should contain "testee.rzy:9:7: Error:"


@fixme
Scenario: a case range can not be used when it is used in another branch
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: R{0,100});
    
    elementary
      inp: slot(x: R{0,100})
        case x of
          0..2:
          end
          1..3:
          end
        end
      end
    end

    """

  When I start rizzly with the file "testee.rzy"
  
  Then I expect an error code
  And stderr should contain "testee.rzy:9:7: Error:"

