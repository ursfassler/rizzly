Feature: Use arithmetic multiplication in Rizzly
  As a developer
  I want to use arithmetic multiplication
  In order to multiply 2 numbers


Scenario Outline: multiply 2 values
  Given we have a file "multiply.rzy" with the content:
    """
    Multiply = Component
      op : response(left, right: R{-10,10}):R{-100,100};
    
    elementary
      op : response(left, right: R{-10,10}):R{-100,100}
        return left * right;
      end
    end

    """

  When I succesfully compile "multiply.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |   -1 |    -1 |      1 |
    |   -1 |     0 |      0 |
    |   -1 |     1 |     -1 |
    |    0 |    -1 |      0 |
    |    0 |     0 |      0 |
    |    0 |     1 |      0 |
    |    1 |    -1 |     -1 |
    |    1 |     0 |      0 |
    |    1 |     1 |      1 |
    |    7 |     8 |     56 |
    |   -9 |    -5 |     45 |
    |    6 |    -5 |    -30 |



