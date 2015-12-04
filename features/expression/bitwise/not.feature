Feature: Use bitwise not in Rizzly
  As a developer
  I want to use bitwase not
  In order to have it


Scenario: bitwise not needs values in the full range of power of 2 
  Given we have a file "bitnot.rzy" with the content:
    """
    Bitnot = Component
      op : response(value: R{0,10}):R{0,15};
    
    elementary
      op : response(value: R{0,10}):R{0,15}
        return not value;
      end
    end

    """

  When I start rizzly with the file "bitnot.rzy"
  
  Then I expect an error code
  And stderr should contain "bitnot.rzy:6:12: Error: not only allowed for R{0,2^n-1}"


Scenario: the return type of a bitwise not is in the same range as the argument
  Given we have a file "bitnot.rzy" with the content:
    """
    Bitnot = Component
      op : response(value: R{0,15}):R{0,10};
    
    elementary
      op : response(value: R{0,15}):R{0,10}
        return not value;
      end
    end

    """

  When I start rizzly with the file "bitnot.rzy"
  
  Then I expect an error code
  And stderr should contain "bitnot.rzy:6:5: Error: Data type to big or incompatible to return: R{0,10} := R{0,15}"


Scenario Outline: bitwise not
  Given we have a file "bitnot.rzy" with the content:
    """
    Bitnot = Component
      op : response(value: R{0,15}):R{0,15};
    
    elementary
      op : response(value: R{0,15}):R{0,15}
        return not value;
      end
    end

    """

  When I succesfully compile "bitnot.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<value>) = <result>

  Examples:
    | value | result |
    |     0 |     15 |
    |     1 |     14 |
    |     7 |      8 |
    |     9 |      6 |
    |    14 |      1 |
    |    15 |      0 |



