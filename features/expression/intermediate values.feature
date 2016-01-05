Feature: Use intermediate values in Rizzly
  As a developer
  I want to intermediate values
  In order to use them in expressions


Scenario Outline: scalar intermediate value
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      read : response():R{-100,100};
    
    elementary
      read : response():R{-100,100}
        return <value>;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request read() = <value>

    Examples:
      | value |
      |     0 |
      |     1 |
      |    -1 |
      |    42 |
      |   100 |
      |   -57 |
      |  -100 |


#TODO implement
@fixme
Scenario Outline: string intermediate value
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      read : response():String;
    
    elementary
      read : response():String
        return '<value>';
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request read() = '<value>'

  Examples:
    | value      |
    |       test |
    | hallo welt |


Scenario Outline: boolean intermediate value
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      read : response():Boolean;
    
    elementary
      read : response():Boolean
        return <value>;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request read() = <value>

    Examples:
      | value |
      | False |
      |  True |

