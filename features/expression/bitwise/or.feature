Feature: Use bitwise or operation in Rizzly
  As a developer
  I want to use a bitwise or operation
  In order to have it


Scenario: arguments have to be positive
  Given we have a file "bitor.rzy" with the content:
    """
    Bitor = Component
      op : response(left, right: R{-1,1}):R{-10,10};
    
    elementary
      op : response(left, right: R{-1,1}):R{-10,10}
        return left or right;
      end
    end

    """

  When I start rizzly with the file "bitor.rzy"
  
  Then I expect an error code
  And stderr should contain "bitor.rzy:6:17: Error: or only allowed for positive types"


Scenario: argument range can start from greater than 0
  Given we have a file "bitor.rzy" with the content:
    """
    Bitor = Component
      op : response(left, right: R{10,20}):R{10,31};
    
    elementary
      op : response(left, right: R{10,20}):R{10,31}
        return left or right;
      end
    end

    """

  When I start rizzly with the file "bitor.rzy"
  
  Then I expect no error


Scenario: return type has to be big enough to contain the result
  Given we have a file "bitor.rzy" with the content:
    """
    Bitor = Component
      op : response(left, right: R{10,20}):R{10,20};
    
    elementary
      op : response(left, right: R{10,20}):R{10,20}
        return left or right;
      end
    end

    """

  When I start rizzly with the file "bitor.rzy"
  
  Then I expect an error code
  And stderr should contain "bitor.rzy:6:5: Error: Data type to big or incompatible to return: R{10,20} := R{10,31}"


Scenario Outline: calculate the bitwise and value of 2 numbers
  Given we have a file "bitor.rzy" with the content:
    """
    Bitor = Component
      op : response(left, right: R{0,20}):R{0,31};
    
    elementary
      op : response(left, right: R{0,20}):R{0,31}
        return left or right;
      end
    end

    """

  When I succesfully compile "bitor.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |    0 |     0 |      0 |
    |    0 |     1 |      1 |
    |    1 |     0 |      1 |
    |    1 |     1 |      1 |
    |   19 |     2 |     19 |
    |   19 |     3 |     19 |
    |   19 |     4 |     23 |
    |   19 |     5 |     23 |
    |   19 |    19 |     19 |
    |   19 |    12 |     31 |

