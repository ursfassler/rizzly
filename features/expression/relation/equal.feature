Feature: Use equal relation in Rizzly
  As a developer
  I want to compare 2 values
  In order to check if they are equal


Scenario Outline: compare boolean values for equality
  Given we have a file "equal.rzy" with the content:
    """
    Equal = Component
      op : response(left, right: Boolean):Boolean;
    
    elementary
      op : response(left, right: Boolean):Boolean
        return left = right;
      end
    end

    """

  When I succesfully compile "equal.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    |  left | right | result |
    | False | False |   True |
    | False |  True |  False |
    |  True | False |  False |
    |  True |  True |   True |


Scenario Outline: check if the left value is equal to the right value
  Given we have a file "equal.rzy" with the content:
    """
    Equal = Component
      op : response(left, right: R{-10,10}):Boolean;
    
    elementary
      op : response(left, right: R{-10,10}):Boolean
        return left = right;
      end
    end

    """

  When I succesfully compile "equal.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |   -1 |    -1 |   True |
    |   -1 |     0 |  False |
    |   -1 |     1 |  False |
    |    0 |    -1 |  False |
    |    0 |     0 |   True |
    |    0 |     1 |  False |
    |    1 |    -1 |  False |
    |    1 |     0 |  False |
    |    1 |     1 |   True |
    |   19 |     2 |  False |
    |    3 |    16 |  False |
    |   -4 |    -4 |   True |

