Feature: generate optimized code
  As a developer
  I want the compiler to produce optimized code
  In order to make the code smaller even when the following compiler is not so good

#TODO how can we test this optimizations?


#TODO add the hint
@fixme
Scenario: optimize type related compare tautologies
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      get: response(x: R{0,100}):Boolean;

    elementary
      get: response(x: R{0,100}):Boolean
        return x >= 0;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"
  
  Then I expect no error
  And stdout should contain "testee.rzy:6:14: Hint: comparsion always evaluates to True"



