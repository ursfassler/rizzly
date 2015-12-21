Feature: Use constants in the elementary implementation
  As a developer
  I want to use constants inside a elementary implementation
  In order give a local constant thing a name that is not seen from everywhere


Scenario: Declare a constant inside the component
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    elementary
      Answer : const R{0,99} = 42;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: For a constant inside the component, I can ommit the type
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    elementary
      Answer : const = 42;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: The component constant can be accessed within the component
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      get: response():R{0,100};

    elementary
      Answer : const R{0,99} = 42;

      get: response():R{0,100}
        return Answer;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42


#TODO implement this
@fixme
Scenario: The constant can not be written
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();

    elementary
      Answer : const R{0,99} = 42;

      tick: slot()
        Answer := 23;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:8:12: Error: TODO some meaningfull error message"


Scenario: I can use compile time types for constants
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      get: response():R{0,100};

    elementary
      Answer : const Natural = 42;

      get: response():R{0,100}
        return Answer;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42

