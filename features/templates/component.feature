Feature: Declare template component
  As a developer
  I want to write template components
  In order to reduce code duplication and increase reusability


Scenario: declare and instantiate a template component with a type template parameter
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{T:Type{Integer}} = Component
    elementary
    end
    
    Dummy = Component
    composition
      testee1: Testee{R{0,100}};
      testee2: Testee{R{-20,42}};
      testee3: Testee{R{-100000,100000}};
    end

    """

  When I start rizzly with the file "dummy.rzy"

  Then I expect no error


Scenario: use a template component with a type template parameter
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{T:Type{Integer}} = Component
      get: response():T;
    elementary
      get: response():T
        return 42;
      end
    end
    
    Dummy = Component
      get: response():R{0,100};
    
    composition
      testee: Testee{R{0,100}};
      
      get -> testee.get;
    end

    """

  When I succesfully compile "dummy.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42


Scenario: declare a template component with a value template parameter
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{T:Integer} = Component
    elementary
    end
    
    Dummy = Component
    composition
      testee1: Testee{0};
      testee2: Testee{-20};
      testee3: Testee{100000};
    end

    """

  When I start rizzly with the file "dummy.rzy"

  Then I expect no error


Scenario: use a template component with a value template parameter
  Given we have a file "dummy.rzy" with the content:
    """
    Testee{T:Integer} = Component
      get: response():R{0,100};
    elementary
      get: response():R{0,100}
        return T;
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

