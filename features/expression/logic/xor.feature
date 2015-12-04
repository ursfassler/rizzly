Feature: Use logic xor operation in Rizzly
  As a developer
  I want to use a logic xor operation
  In order to construct predicates


Scenario Outline: do a logic xor
  Given we have a file "logicxor.rzy" with the content:
    """
    Logicxor = Component
      op : response(left, right: Boolean):Boolean;
    
    elementary
      op : response(left, right: Boolean):Boolean
        return left xor right;
      end
    end

    """

  When I succesfully compile "logicxor.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    |  left | right | result |
    | False | False |  False |
    | False |  True |   True |
    |  True | False |   True |
    |  True |  True |  False |

