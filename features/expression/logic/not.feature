Feature: Use logic not in Rizzly
  As a developer
  I want to use logic not
  In order to invert a predicate


Scenario Outline: logic not
  Given we have a file "logicnot.rzy" with the content:
    """
    Logicnot = Component
      op : response(value: Boolean):Boolean;
    
    elementary
      op : response(value: Boolean):Boolean
        return not value;
      end
    end

    """

  When I succesfully compile "logicnot.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<value>) = <result>

  Examples:
    | value | result |
    | False |   True |
    |  True |  False |

