Feature: Use arithmetic division in Rizzly
  As a developer
  I want to use arithmetic division
  In order to divide a number form another


#TODO implement negative numbers
@fixme
Scenario Outline: divide 2 values
  Given we have a file "divide.rzy" with the content:
    """
    Divide = Component
      op : response(left: R{0,100}; right: R{0,10}):R{0,100};
    
    elementary
      op : response(left:R{0,100}; right: R{0,10}):R{0,100}
        return left / right;
      end
    end

    """

  When I succesfully compile "divide.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |    0 |     1 |      0 |
    |    1 |     1 |      1 |
    |   56 |     8 |      7 |
    |   45 |     5 |      9 |
    |   30 |     5 |      6 |
    |   99 |     1 |     99 |
    |   99 |    99 |      1 |
    |    6 |     1 |      6 |
    |    6 |     2 |      3 |
    |    6 |     3 |      2 |
    |    6 |     4 |      1 |
    |    6 |     5 |      1 |
    |    6 |     6 |      1 |
    |    6 |     7 |      0 |
    |    6 |     8 |      0 |
    |    6 |     9 |      0 |
    |    6 |    10 |      0 |
    |    6 |    11 |      0 |
    |    6 |    12 |      0 |

