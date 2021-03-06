Feature: Use arithmetic subtraction in Rizzly
  As a developer
  I want to use arithmetic subtraction
  In order to subtract a number form another


Scenario Outline: subtract 2 values
  Given we have a file "subtract.rzy" with the content:
    """
    Subtract = Component
      op : response(left, right: R{-10,10}):R{-100,100};
    
    elementary
      op : response(left, right: R{-10,10}):R{-100,100}
        return left - right;
      end
    end

    """

  When I succesfully compile "subtract.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |   -1 |    -1 |      0 |
    |   -1 |     0 |     -1 |
    |   -1 |     1 |     -2 |
    |    0 |    -1 |      1 |
    |    0 |     0 |      0 |
    |    0 |     1 |     -1 |
    |    1 |    -1 |      2 |
    |    1 |     0 |      1 |
    |    1 |     1 |      0 |
    |    7 |     8 |     -1 |
    |   -9 |    -5 |     -4 |
    |    6 |    -5 |     11 |



