Feature: Use bitwise xor operation in Rizzly
  As a developer
  I want to use a bitwise xor operation
  In order to do crypto stuff


Scenario: arguments have to be positive
  Given we have a file "bitxor.rzy" with the content:
    """
    Bitxor = Component
      op : response(left, right: R{-1,1}):R{-10,10};
    
    elementary
      op : response(left, right: R{-1,1}):R{-10,10}
        return left xor right;
      end
    end

    """

  When I start rizzly with the file "bitxor.rzy"
  
  Then I expect an error code
  And stderr should contain "bitxor.rzy:6:17: Error: xor only allowed for positive types"


Scenario: argument range can start from greater than 0
  Given we have a file "bitxor.rzy" with the content:
    """
    Bitxor = Component
      op : response(left, right: R{10,20}):R{0,31};
    
    elementary
      op : response(left, right: R{10,20}):R{0,31}
        return left xor right;
      end
    end

    """

  When I start rizzly with the file "bitxor.rzy"
  
  Then I expect no error


Scenario: return upper bound has to be big enough to contain the result
  Given we have a file "bitxor.rzy" with the content:
    """
    Bitxor = Component
      op : response(left, right: R{10,20}):R{0,20};
    
    elementary
      op : response(left, right: R{10,20}):R{0,20}
        return left xor right;
      end
    end

    """

  When I start rizzly with the file "bitxor.rzy"
  
  Then I expect an error code
  And stderr should contain "bitxor.rzy:6:5: Error: Data type to big or incompatible to return: R{0,20} := R{0,31}"


Scenario: return lower bound has to be big enough to contain the result
  Given we have a file "bitxor.rzy" with the content:
    """
    Bitxor = Component
      op : response(left, right: R{10,20}):R{10,31};
    
    elementary
      op : response(left, right: R{10,20}):R{10,31}
        return left xor right;
      end
    end

    """

  When I start rizzly with the file "bitxor.rzy"
  
  Then I expect an error code
  And stderr should contain "bitxor.rzy:6:5: Error: Data type to big or incompatible to return: R{10,31} := R{0,31}"


Scenario Outline: calculate the bitwise and value of 2 numbers
  Given we have a file "bitxor.rzy" with the content:
    """
    Bitxor = Component
      op : response(left, right: R{0,20}):R{0,31};
    
    elementary
      op : response(left, right: R{0,20}):R{0,31}
        return left xor right;
      end
    end

    """

  When I succesfully compile "bitxor.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |    0 |     0 |      0 |
    |    0 |     1 |      1 |
    |    1 |     0 |      1 |
    |    1 |     1 |      0 |
    |   19 |     2 |     17 |
    |   19 |     3 |     16 |
    |   19 |     4 |     23 |
    |   19 |     5 |     22 |
    |   19 |    19 |      0 |
    |   19 |    12 |     31 |

