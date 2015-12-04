Feature: Use logic or operation in Rizzly
  As a developer
  I want to use a logic or operation
  In order to construct predicates


Scenario Outline: do a logic or
  Given we have a file "logicor.rzy" with the content:
    """
    Logicor = Component
      op : response(left, right: Boolean):Boolean;
    
    elementary
      op : response(left, right: Boolean):Boolean
        return left or right;
      end
    end

    """

  When I succesfully compile "logicor.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    |  left | right | result |
    | False | False |  False |
    | False |  True |   True |
    |  True | False |   True |
    |  True |  True |   True |

