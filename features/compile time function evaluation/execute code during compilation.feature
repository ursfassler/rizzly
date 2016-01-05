Feature: Evaluate code during compilation
  As a developer
  I want the compiler to evaluate code
  In order to make the code smaller and runtime faster when I have non trivial initial values


Scenario: initialize a component constant with a function result
  Given we have a file "testee.rzy" with the content:
    """
    calcAnswer = function():R{0,100}
      return 42;
    end
    
    Testee = Component
      get: response():R{0,100};

    elementary
      Answer : const = calcAnswer();

      get: response():R{0,100}
        return Answer;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42


#TODO remove component
Scenario: initialize a global constant with a function result
  Given we have a file "testee.rzy" with the content:
    """
    calcAnswer = function():R{0,100}
      return 42;
    end

    Answer : const = calcAnswer();
    
    Testee = Component
      get: response():R{0,100};
    elementary
      get: response():R{0,100}
        return Answer;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42


Scenario: initialize a component constant with a templated function result
  Given we have a file "testee.rzy" with the content:
    """
    calcAnswer{S:R{0,100}} = function():R{0,100}
      return S;
    end
    
    Testee = Component
      get: response():R{0,100};

    elementary
      Answer : const = calcAnswer{42}();

      get: response():R{0,100}
        return Answer;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42


Scenario: initialize a global constant with a templated function result
  Given we have a file "testee.rzy" with the content:
    """
    calcAnswer{S:R{0,100}} = function():R{0,100}
      return S;
    end
    
    Answer : const = calcAnswer{42}();
      
    Testee = Component
      get: response():R{0,100};

    elementary
      get: response():R{0,100}
        return Answer;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42


Scenario: Use a template argument as a constant initializer
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{S:R{0,100}} = Component
      get: response():R{0,100};

    elementary
      Answer : const = S;

      get: response():R{0,100}
        return Answer;
      end
    end

    Dummy = Component
      get: response():R{0,100};
    composition
      testee: Testee{42};
      get -> testee.get;
    end

    """

  When I succesfully compile "dummy.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42

