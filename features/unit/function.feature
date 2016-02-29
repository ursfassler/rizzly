Feature: Use functions in a program
  As a developer
  I want to split my application into separate functions
  In order to make the code cleaner and reusable


#TODO remove component
Scenario: declare a function in a unit
  Given we have a file "testee.rzy" with the content:
    """
    tuwas = function(a: R{0,100}):R{0,100}
      return a;
    end

    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


#TODO remove component
Scenario: a function needs a return value
  Given we have a file "testee.rzy" with the content:
    """
    tuwas = function(a: R{0,100})
      return a;
    end

    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:2:3: Error: expected COLON got RETURN"


#TODO remove component
Scenario: a function can have multiple return values
  Given we have a file "testee.rzy" with the content:
    """
    tuwas = function(a, b: R{0,10}):(a, b: R{0,10})
      return (a, b);
    end

    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: The function return values can be accesses by name
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op: response(a, b: R{0,10}):R{0,10};

    elementary
      tuwas = function(a, b: R{0,10}):(a, b: R{0,10})
        return (a, b);
      end

      op: response(a, b: R{0,10}):R{0,10}
        return tuwas(a, b).b;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(3, 6) = 6


Scenario: The function return values can be assigned to variables
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      op: response(a, b: R{0,10}):R{0,10};

    elementary
      tuwas = function(a, b: R{0,10}):(a, b: R{0,10})
        return (a, b);
      end

      op: response(a, b: R{0,10}):R{0,10}
        c, d : R{0,10};
        c, d := tuwas(a, b);
        return d;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request op(3, 6) = 6


Scenario: Declare a recursive function call
  Given we have a file "testee.rzy" with the content:
    """
    foo = function():Boolean
      return foo();
    end
    
    Testee = Component
      ind : response():Boolean;

    elementary
      ind : response():Boolean
        return foo();
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: Declare a recursive function call over 2 functions
  Given we have a file "testee.rzy" with the content:
    """
    foo = function():Boolean
      return bar();
    end
    
    bar = function():Boolean
      return foo();
    end
    
    Testee = Component
      ind : response():Boolean;

    elementary
      ind : response():Boolean
        return foo();
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error

