Feature: Use bitwise and operation in Rizzly
  As a developer
  I want to use a bitwise and operation
  In order to mask variables


Scenario: arguments have to be positive
  Given we have a file "bitand.rzy" with the content:
    """
    Bitand = Component
      op : response(left, right: R{-1,1}):R{-10,10};
    
    elementary
      op : response(left, right: R{-1,1}):R{-10,10}
        return left and right;
      end
    end

    """

  When I start rizzly with the file "bitand.rzy"
  
  Then I expect an error code
  And stderr should contain "bitand.rzy:6:17: Error: and only allowed for positive types"


Scenario: argument range can start from greater than 0
  Given we have a file "bitand.rzy" with the content:
    """
    Bitand = Component
      op : response(left, right: R{10,20}):R{0,20};
    
    elementary
      op : response(left, right: R{10,20}):R{0,20}
        return left and right;
      end
    end

    """

  When I start rizzly with the file "bitand.rzy"
  
  Then I expect no error


Scenario: return type has to be big enough to contain the result
  Given we have a file "bitand.rzy" with the content:
    """
    Bitand = Component
      op : response(left, right: R{10,20}):R{0,19};
    
    elementary
      op : response(left, right: R{10,20}):R{0,19}
        return left and right;
      end
    end

    """

  When I start rizzly with the file "bitand.rzy"
  
  Then I expect an error code
  And stderr should contain "bitand.rzy:6:5: Error: Data type to big or incompatible to return: R{0,19} := R{0,20}"


Scenario: return type is reduced according to the operation and argument types
  Given we have a file "bitand.rzy" with the content:
    """
    Bitand = Component
      op : response(left: R{0,255}; right: R{0,7}):R{0,7};
    
    elementary
      op : response(left: R{0,255}; right: R{0,7}):R{0,7}
        return left and right;
      end
    end

    """

  When I start rizzly with the file "bitand.rzy"
  
  Then I expect no error


Scenario Outline: calculate the bitwise and value of 2 numbers
  Given we have a file "bitand.rzy" with the content:
    """
    Bitand = Component
      op : response(left, right: R{0,20}):R{0,20};
    
    elementary
      op : response(left, right: R{0,20}):R{0,20}
        return left and right;
      end
    end

    """

  When I succesfully compile "bitand.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |    0 |     0 |      0 |
    |    0 |     1 |      0 |
    |    1 |     0 |      0 |
    |    1 |     1 |      1 |
    |   19 |     2 |      2 |
    |   19 |     3 |      3 |
    |   19 |     4 |      0 |
    |   19 |     5 |      1 |
    |   19 |    19 |     19 |
    |   19 |    20 |     16 |

