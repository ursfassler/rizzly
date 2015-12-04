Feature: Use arithmetic modulo in Rizzly
  As a developer
  I want to use arithmetic modulo
  In order to calculata a number modulo another


Scenario: modulo has to be positive
  Given we have a file "modulo.rzy" with the content:
    """
    Modulo = Component
      op : response(left: R{0,10}; right: R{-1,1}):R{-10,10};
    
    elementary
      op : response(left: R{0,10}; right: R{-1,1}):R{-10,10}
        return left mod right;
      end
    end

    """

  When I start rizzly with the file "modulo.rzy"
  
  Then I expect an error code
  And stderr should contain "modulo.rzy:6:17: Error: right side of mod has to be greater than 0"


Scenario: modulo has to be greater than 0
  Given we have a file "modulo.rzy" with the content:
    """
    Modulo = Component
      op : response(left: R{0,10}; right: R{0,10}):R{0,10};
    
    elementary
      op : response(left: R{0,10}; right: R{0,10}):R{0,10}
        return left mod right;
      end
    end

    """

  When I start rizzly with the file "modulo.rzy"
  
  Then I expect an error code
  And stderr should contain "modulo.rzy:6:17: Error: right side of mod has to be greater than 0"


Scenario: return type of modulo is between 0 and modulo-1
  Given we have a file "modulo.rzy" with the content:
    """
    Modulo = Component
      op : response(left: R{0,100}; right: R{1,10}):R{0,9};
    
    elementary
      op : response(left: R{0,100}; right: R{1,10}):R{0,9}
        return left mod right;
      end
    end

    """

  When I start rizzly with the file "modulo.rzy"
  
  Then I expect no error


#TODO implement negative numbers
@fixme
Scenario Outline: calculate modulo of 2 values
  Given we have a file "modulo.rzy" with the content:
    """
    Modulo = Component
      op : response(left: R{0,100}; right: R{1,10}):R{0,100};
    
    elementary
      op : response(left:R{0,100}; right: R{1,10}):R{0,100}
        return left mod right;
      end
    end

    """

  When I succesfully compile "modulo.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |    0 |     1 |      0 |
    |    1 |     1 |      0 |
    |   56 |     5 |      1 |
    |   45 |     7 |      3 |
    |   30 |    19 |     11 |
    |   99 |    99 |      0 |
    |    6 |     1 |      0 |
    |    6 |     2 |      0 |
    |    6 |     3 |      0 |
    |    6 |     4 |      2 |
    |    6 |     5 |      1 |
    |    6 |     6 |      0 |
    |    6 |     7 |      6 |
    |    6 |     8 |      6 |
    |    6 |     9 |      6 |
    |    6 |    10 |      6 |
    |    6 |    11 |      6 |
    |    6 |    12 |      6 |

