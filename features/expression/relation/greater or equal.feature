Feature: Use greater or equal relation in Rizzly
  As a developer
  I want to compare 2 values
  In order to check if the left one greater or equal than the right one


Scenario: greater or equal can not be used with boolean types
  Given we have a file "greaterequal.rzy" with the content:
    """
    Greaterequal = Component
      op : response(left, right: Boolean):Boolean;
    
    elementary
      op : response(left, right: Boolean):Boolean
        return left >= right;
      end
    end

    """

  When I start rizzly with the file "greaterequal.rzy"
  
  Then I expect an error code
  And stderr should contain "greaterequal.rzy:6:12: Fatal: Expected range type, got Boolean"


Scenario Outline: check if the left value is greater or equal than the right value
  Given we have a file "greaterequal.rzy" with the content:
    """
    Greaterequal = Component
      op : response(left, right: R{-10,10}):Boolean;
    
    elementary
      op : response(left, right: R{-10,10}):Boolean
        return left >= right;
      end
    end

    """

  When I succesfully compile "greaterequal.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |   -1 |    -1 |   True |
    |   -1 |     0 |  False |
    |   -1 |     1 |  False |
    |    0 |    -1 |   True |
    |    0 |     0 |   True |
    |    0 |     1 |  False |
    |    1 |    -1 |   True |
    |    1 |     0 |   True |
    |    1 |     1 |   True |
    |   19 |     2 |   True |
    |    3 |    16 |  False |
    |   -4 |    -4 |   True |


#TODO fix error message
@fixme
Scenario: tuples have no greater or equal relation
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(left1, left2, right1, right2: R{0,10}):Boolean;
    
    elementary
      op : response(left1, left2, right1, right2: R{0,10}):Boolean
        return (left1, left2) >= (right1, right2);
      end
    end

    """

  When I start rizzly with the file "testee.rzy"
  
  Then I expect an error code
  And stderr should contain "testee.rzy:6:12: Fatal: Expected range type, got tuple"

