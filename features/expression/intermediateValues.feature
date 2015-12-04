Feature: Use intermediate values in Rizzly
  As a developer
  I want to intermediate values
  In order to use them in expressions


Scenario Outline: return a scalar value
  Given we have a file "scalar.rzy" with the content:
    """
    Scalar = Component
      read : response():R{-100,100};
    
    elementary
      read : response():R{-100,100}
        return <value>;
      end
    end

    """

  When I succesfully compile "scalar.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request read() = <value>

    Examples:
      | value |
      |     0 |
      |     1 |
      |    42 |
      |    57 |
      |   100 |
      |    -1 |
      |   -42 |
      |   -57 |
      |  -100 |


