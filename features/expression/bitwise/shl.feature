Feature: Use bitwise shift left operation in Rizzly
  As a developer
  I want to use a bitwise shift left operation
  In order to have it


Scenario: shift left can not be used with boolean types
  Given we have a file "bitshl.rzy" with the content:
    """
    Bitshl = Component
      op : response(left, right: Boolean):Boolean;
    
    elementary
      op : response(left, right: Boolean):Boolean
        return left shl right;
      end
    end

    """

  When I start rizzly with the file "bitshl.rzy"
  
  Then I expect an error code
  And stderr should contain "bitshl.rzy:6:12: Fatal: Expected range type, got Boolean"


Scenario: arguments have to be positive
  Given we have a file "bitshl.rzy" with the content:
    """
    Bitshl = Component
      op : response(left, right: R{-1,1}):R{-10,10};
    
    elementary
      op : response(left, right: R{-1,1}):R{-10,10}
        return left shl right;
      end
    end

    """

  When I start rizzly with the file "bitshl.rzy"
  
  Then I expect an error code
  And stderr should contain "bitshl.rzy:6:17: Error: shl only allowed for positive types"


Scenario: argument range can start from greater than 0
  Given we have a file "bitshl.rzy" with the content:
    """
    Bitshl = Component
      op : response(left, right: R{2,4}):R{0,64};
    
    elementary
      op : response(left, right: R{2,4}):R{0,64}
        return left shl right;
      end
    end

    """

  When I start rizzly with the file "bitshl.rzy"
  
  Then I expect no error


Scenario: return upper bound has to be big enough to contain the result
  Given we have a file "bitshl.rzy" with the content:
    """
    Bitshl = Component
      op : response(left, right: R{0,4}):R{0,20};
    
    elementary
      op : response(left, right: R{0,4}):R{0,20}
        return left shl right;
      end
    end

    """

  When I start rizzly with the file "bitshl.rzy"
  
  Then I expect an error code
  And stderr should contain "bitshl.rzy:6:5: Error: Data type to big or incompatible to return: R{0,20} := R{0,64}"


Scenario: return lower bound has to be big enough to contain the result
  Given we have a file "bitshl.rzy" with the content:
    """
    Bitshl = Component
      op : response(left, right: R{2,4}):R{20,64};
    
    elementary
      op : response(left, right: R{2,4}):R{20,64}
        return left shl right;
      end
    end

    """

  When I start rizzly with the file "bitshl.rzy"
  
  Then I expect an error code
  And stderr should contain "bitshl.rzy:6:5: Error: Data type to big or incompatible to return: R{20,64} := R{8,64}"


Scenario Outline: calculate the bitwise and value of 2 numbers
  Given we have a file "bitshl.rzy" with the content:
    """
    Bitshl = Component
      op : response(left, right: R{0,4}):R{0,64};
    
    elementary
      op : response(left, right: R{0,4}):R{0,64}
        return left shl right;
      end
    end

    """

  When I succesfully compile "bitshl.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |    0 |     0 |      0 |
    |    0 |     1 |      0 |
    |    1 |     0 |      1 |
    |    1 |     1 |      2 |
    |    1 |     2 |      4 |
    |    4 |     4 |     64 |
    |    3 |     2 |     12 |

