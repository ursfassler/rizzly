Feature: Declare template functions
  As a developer
  I want to write template functions
  In order to reduce code duplication and increase reusability


Scenario: declare a template function with a type template parameter
  Given we have a file "testee.rzy" with the content:
    """
    foo{T:Type{Integer}} = function(x: T):T
      return T;
    end
    
    Testee = Component
      op : response(value: R{0,100}):R{0,100};
    
    elementary
      op : response(value: R{0,100}):R{0,100}
        return foo{R{0,100}}(value);
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: declare a template function with a value template parameter
  Given we have a file "testee.rzy" with the content:
    """
    foo{T:R{0,100}} = function():R{0,100}
      return T;
    end
    
    Testee = Component
      op : response():R{0,100};
    
    elementary
      op : response():R{0,100}
        return foo{42}();
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error

