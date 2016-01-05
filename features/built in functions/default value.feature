Feature: get default values for types
  As a developer
  I want to initialize my variables with default values
  In order to initialize templated variables


#TODO add array
#TODO add enum
#TODO add record
#TODO add union
Scenario Outline: deault values for simple types
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      get : response():<type>;
    
    elementary
      get : response():<type>
        return default{<type>}();
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = <value>

  Examples:
    | type       | value |
    | R{-10,10}  |     0 |
    | R{0,100}   |     0 |
    | R{10,20}   |    10 |
    | R{-10,0}   |     0 |
    | R{-20,-10} |   -10 |
    | Boolean    | False |

