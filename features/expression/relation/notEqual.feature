Feature: Use not equal relation in Rizzly
  As a developer
  I want to compare 2 values
  In order to check if they are not equal


Scenario Outline: compare boolean values for inequality
  Given we have a file "notequal.rzy" with the content:
    """
    Notequal = Component
      op : response(left, right: Boolean):Boolean;
    
    elementary
      op : response(left, right: Boolean):Boolean
        return left <> right;
      end
    end

    """

  When I succesfully compile "notequal.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    |  left | right | result |
    | False | False |  False |
    | False |  True |   True |
    |  True | False |   True |
    |  True |  True |  False |


Scenario Outline: check if the left value is not equal to the right value
  Given we have a file "notequal.rzy" with the content:
    """
    Notequal = Component
      op : response(left, right: R{-10,10}):Boolean;
    
    elementary
      op : response(left, right: R{-10,10}):Boolean
        return left <> right;
      end
    end

    """

  When I succesfully compile "notequal.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |   -1 |    -1 |  False |
    |   -1 |     0 |   True |
    |   -1 |     1 |   True |
    |    0 |    -1 |   True |
    |    0 |     0 |  False |
    |    0 |     1 |   True |
    |    1 |    -1 |   True |
    |    1 |     0 |   True |
    |    1 |     1 |  False |
    |   19 |     2 |   True |
    |    3 |    16 |   True |
    |   -4 |    -4 |  False |

