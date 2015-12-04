Feature: Use unary minus in Rizzly
  As a developer
  I want to use unary minus
  In order to get the negative value


Scenario Outline: unary minus
  Given we have a file "minus.rzy" with the content:
    """
    Minus = Component
      op : response(value: R{-10,10}):R{-10,10};
    
    elementary
      op : response(value: R{-10,10}):R{-10,10}
        return -value;
      end
    end

    """

  When I succesfully compile "minus.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<value>) = <result>

  Examples:
    | value | result |
    |     0 |      0 |
    |     1 |     -1 |
    |    -1 |      1 |
    |     7 |     -7 |
    |    -9 |      9 |


#TODO shouldn't it be possible to use "--value"?
@fixme
Scenario Outline: double unary minus
  Given we have a file "minus.rzy" with the content:
    """
    Minus = Component
      op : response(value: R{-10,10}):R{-10,10};
    
    elementary
      op : response(value: R{-10,10}):R{-10,10}
        return -(-value);
      end
    end

    """

  When I succesfully compile "minus.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<value>) = <result>

  Examples:
    | value | result |
    |     0 |      0 |
    |     1 |      1 |
    |    -1 |     -1 |
    |     7 |      7 |
    |    -9 |     -9 |


Scenario: return type has to be big enough
  Given we have a file "minus.rzy" with the content:
    """
    Minus = Component
      op : response(value: R{3,5}):R{0,0};
    
    elementary
      op : response(value: R{3,5}):R{0,0}
        return -value;
      end
    end

    """

  When I start rizzly with the file "minus.rzy"
  
  Then I expect an error code
  And stderr should contain "minus.rzy:6:5: Error: Data type to big or incompatible to return: R{0,0} := R{-5,-3}"

