Feature: Declare templates
  As a developer
  I want to write templates
  In order to reduce code duplication and increase reusability


#TODO implement/fix this
@fixme
Scenario: Use a template type to further specify another template value
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{T:Type{Integer}; S:T} = Component
    elementary
    end
    
    Dummy = Component
    composition
      testee1: Testee{R{0,100}, 42};
      testee2: Testee{R{-20,42}, -10};
      testee3: Testee{R{-100000,100000}, 12345};
    end

    """

  When I start rizzly with the file "dummy.rzy"

  Then I expect no error

