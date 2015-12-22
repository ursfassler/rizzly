Feature: Use arithmetic addition in Rizzly
  As a developer
  I want to use arithmetic additions
  In order to add 2 numbers


Scenario Outline: add 2 values
  Given we have a file "add.rzy" with the content:
    """
    Add = Component
      op : response(left, right: R{-10,10}):R{-100,100};
    
    elementary
      op : response(left, right: R{-10,10}):R{-100,100}
        return left + right;
      end
    end

    """

  When I succesfully compile "add.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left>, <right>) = <result>

  Examples:
    | left | right | result |
    |   -1 |    -1 |     -2 |
    |   -1 |     0 |     -1 |
    |   -1 |     1 |      0 |
    |    0 |    -1 |     -1 |
    |    0 |     0 |      0 |
    |    0 |     1 |      1 |
    |    1 |    -1 |      0 |
    |    1 |     0 |      1 |
    |    1 |     1 |      2 |
    |    7 |     8 |     15 |
    |   -9 |    -5 |    -14 |
    |    6 |    -5 |      1 |


Scenario: the resulting type can hold the result
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(left, right: R{-10,10}):R{-10,10};
    
    elementary
      op : response(left, right: R{-10,10}):R{-10,10}
        return left + right;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:6:5: Error: Data type to big or incompatible to return: R{-10,10} := R{-20,20}"

