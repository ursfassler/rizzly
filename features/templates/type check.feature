Feature: Check if the type of the template argument is correct
  As a developer
  I want that the compiler checks if the template types are correct
  In order to find errors early


#TODO check for that error
@fixme
Scenario: A natural type can only take positive range types
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{T:Type{Natural}} = Component
    elementary
    end
    
    Dummy = Component
    composition
      testee: Testee{R{-10,10}};
    end

    """

  When I start rizzly with the file "dummy.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:7:18: Error: TODO some nice error message"


#TODO check for that error
@fixme
Scenario: Do not allow values when a type is expected
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{T:Type{Any}} = Component
    elementary
    end
    
    Dummy = Component
    composition
      testee: Testee{10};
    end

    """

  When I start rizzly with the file "dummy.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:7:18: Error: TODO some nice error message"


#TODO add position in error message
@fixme
Scenario: Do not allow types when a value is expected
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{T:Integer} = Component
    elementary
    end
    
    Dummy = Component
    composition
      testee: Testee{R{0,10}};
    end

    """

  When I start rizzly with the file "dummy.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:7:18: Error: Expected value, got type"


#TODO check for that error
@fixme
Scenario: Natural argument has to be grater or equal 0
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{T:Natural} = Component
    elementary
    end
    
    Dummy = Component
    composition
      testee: Testee{-10};
    end

    """

  When I start rizzly with the file "dummy.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:7:18: Error: TODO some nice error message"


#TODO check for that error
@fixme
Scenario: Find to big range argument
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{T:Type{R{10,20}}} = Component
    elementary
    end
    
    Dummy = Component
    composition
      testee: Testee{R{7,15}};
    end

    """

  When I start rizzly with the file "dummy.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:7:18: Error: TODO some nice error message"

