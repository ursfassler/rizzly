Feature: declare and use enumerators
  As a developer
  I want to use enumerators
  In order to access related related items by name


#TODO remove component
Scenario: declare a simple enumerator type
  Given we have a file "testee.rzy" with the content:
    """
    Color = Enum
      Red;
      Green;
      Blue;
    end
    
    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


#TODO this should work
@fixme
Scenario: use an enumerator as type for a variable
  Given we have a file "testee.rzy" with the content:
    """
    Color = Enum
      Red;
      Green;
      Blue;
    end
    
    Testee = Component
    elementary
      color: Color = Color.Green;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


#TODO raise error with a good message
@fixme
Scenario: A numeric value is incompatible with an enumerator
  Given we have a file "testee.rzy" with the content:
    """
    Color = Enum
      Red;
      Green;
      Blue;
    end
    
    Testee = Component
    elementary
      color: Color = 0;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:9:16: Error: TODO error message"


#TODO raise error with a good message
@fixme
Scenario: Enumerators are incompatible
  Given we have a file "testee.rzy" with the content:
    """
    Foo = Enum
      Red;
    end

    Bar = Enum
      Red;
    end
    
    Testee = Component
    elementary
      foo: Foo = Bar.Red;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:9:16: Error: TODO error message"


Scenario: I can use an enumerator in a case
  Given we have a file "testee.rzy" with the content:
    """
    Color = Enum
      Red;
      Green;
      Blue;
    end
    
    Testee = Component
    elementary
      test = function(color: Color):R{0,10}
        case color of
          Color.Red:
            return 3;
          end
          Color.Green .. Color.Blue:
            return 5;
          end
        end
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error

