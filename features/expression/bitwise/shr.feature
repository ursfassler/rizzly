Feature: Use bitwise shift right operation in Rizzly
  As a developer
  I want to use a bitwise shift right operation
  In order to have it


Scenario: shift right can not be used with boolean types
  Given we have a file "bitshr.rzy" with the content:
    """
    Bitshr = Component
      op : response(left, right: Boolean):Boolean;
    
    elementary
      op : response(left, right: Boolean):Boolean
        return left shr right;
      end
    end

    """

  When I start rizzly with the file "bitshr.rzy"
  
  Then I expect an error code
  And stderr should contain "bitshr.rzy:6:12: Fatal: Expected range type, got Boolean"


Scenario: arguments have to be positive
  Given we have a file "bitshr.rzy" with the content:
    """
    Bitshr = Component
      op : response(left, right: R{-1,1}):R{-10,10};
    
    elementary
      op : response(left, right: R{-1,1}):R{-10,10}
        return left shr right;
      end
    end

    """

  When I start rizzly with the file "bitshr.rzy"
  
  Then I expect an error code
  And stderr should contain "bitshr.rzy:6:17: Error: shr only allowed for positive types"


Scenario: argument range can start from greater than 0
  Given we have a file "bitshr.rzy" with the content:
    """
    Bitshr = Component
      op : response(left, right: R{10,20}):R{0,20};
    
    elementary
      op : response(left, right: R{10,20}):R{0,20}
        return left shr right;
      end
    end

    """

  When I start rizzly with the file "bitshr.rzy"
  
  Then I expect no error


Scenario: return upper bound has to be big enough to contain the result
  Given we have a file "bitshr.rzy" with the content:
    """
    Bitshr = Component
      op : response(left, right: R{0,20}):R{0,10};
    
    elementary
      op : response(left, right: R{0,20}):R{0,10}
        return left shr right;
      end
    end

    """

  When I start rizzly with the file "bitshr.rzy"
  
  Then I expect an error code
  And stderr should contain "bitshr.rzy:6:5: Error: Data type to big or incompatible to return: R{0,10} := R{0,20}"


Scenario: return lower bound has to be big enough to contain the result
  Given we have a file "bitshr.rzy" with the content:
    """
    Bitshr = Component
      op : response(left, right: R{0,4}):R{20,64};
    
    elementary
      op : response(left, right: R{0,4}):R{20,64}
        return left shr right;
      end
    end

    """

  When I start rizzly with the file "bitshr.rzy"
  
  Then I expect an error code
  And stderr should contain "bitshr.rzy:6:5: Error: Data type to big or incompatible to return: R{20,64} := R{0,4}"


Scenario Outline: calculate the bitwise shr value of 2 numbers
  Given we have a file "bitshr.rzy" with the content:
    """
    Bitshr = Component
      op : response(left, right: R{0,20}):R{0,20};
    
    elementary
      op : response(left, right: R{0,20}):R{0,20}
        return left shr right;
      end
    end

    """

  When I succesfully compile "bitshr.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |    0 |     0 |      0 |
    |    0 |     1 |      0 |
    |    1 |     0 |      1 |
    |    1 |     1 |      0 |
    |   20 |     1 |     10 |
    |   19 |     1 |      9 |
    |   18 |     1 |      9 |
    |   19 |     2 |      4 |
    |   19 |     3 |      2 |

