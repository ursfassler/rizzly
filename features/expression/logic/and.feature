Feature: Use logic and operation in Rizzly
  As a developer
  I want to use a logic and operation
  In order to construct predicates


Scenario Outline: do a logic and
  Given we have a file "logicand.rzy" with the content:
    """
    Logicand = Component
      op : response(left, right: Boolean):Boolean;
    
    elementary
      op : response(left, right: Boolean):Boolean
        return left and right;
      end
    end

    """

  When I succesfully compile "logicand.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    |  left | right | result |
    | False | False |  False |
    | False |  True |  False |
    |  True | False |  False |
    |  True |  True |   True |

