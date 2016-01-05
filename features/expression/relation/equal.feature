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


Scenario Outline: compare tuples for equality
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(left1, left2, right1, right2: R{0,10}):Boolean;
    
    elementary
      op : response(left1, left2, right1, right2: R{0,10}):Boolean
        return (left1, left2) = (right1, right2);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<left1>, <left2>, <right1>, <right2>) = <result>

  Examples:
    | left1 | left2 | right1 | right2 | result |
    |     0 |     0 |      0 |      0 |   True |
    |     0 |     0 |      0 |      1 |  False |
    |     0 |     0 |      1 |      0 |  False |
    |     0 |     0 |      1 |      1 |  False |
    |     0 |     1 |      0 |      0 |  False |
    |     0 |     1 |      0 |      1 |   True |
    |     0 |     1 |      1 |      0 |  False |
    |     0 |     1 |      1 |      1 |  False |
    |     1 |     0 |      0 |      0 |  False |
    |     1 |     0 |      0 |      1 |  False |
    |     1 |     0 |      1 |      0 |   True |
    |     1 |     0 |      1 |      1 |  False |
    |     1 |     1 |      0 |      0 |  False |
    |     1 |     1 |      0 |      1 |  False |
    |     1 |     1 |      1 |      0 |  False |
    |     1 |     1 |      1 |      1 |   True |


Scenario Outline: Compare a function return value with a tuple
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b: R{0,10}):Boolean;
    
    elementary
      foo = function():(x, y: R{0,10})
        return (2, 3);
      end

      op : response(a, b: R{0,10}):Boolean
        return foo() = (a, b);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<a>, <b>) = <result>

  Examples:
    | a | b | result |
    | 0 | 0 |  False |
    | 2 | 0 |  False |
    | 0 | 3 |  False |
    | 2 | 3 |   True |


#TODO implement underscore as don't care
@fixme
Scenario Outline: Compare tuples with don't cares
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op : response(a, b: R{0,10}):Boolean;
    
    elementary
      op : response(a, b: R{0,10}):Boolean
        return (2, _) = (a, b);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(<a>, <b>) = <result>

  Examples:
    | a | b | result |
    | 0 | 0 |  False |
    | 2 | 0 |   True |
    | 0 | 3 |  False |
    | 2 | 3 |   True |

