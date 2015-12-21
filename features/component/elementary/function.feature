Feature: Declare and use functions in the elementary implementation
  As a developer
  I want to use local functions inside a elementary implementation
  In order make my implementation cleaner by splitting it up


Scenario: Declare a function inside the component
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    elementary
      answer = function():R{0,99}
        return 42;
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: The component local function can be accessed within the component
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      get: response():R{0,100};

    elementary
      answer = function():R{0,99}
        return 42;
      end

      get: response():R{0,100}
        return answer();
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42

