Feature: use aliases for types
  As a developer
  I want to give a type a new name
  In order to write readable programs


#TODO remove component
Scenario: Give a type a new name
  Given we have a file "testee.rzy" with the content:
    """
    U8 = R{0,255};
    
    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: use an alias type as type for a variable
  Given we have a file "testee.rzy" with the content:
    """
    U8 = R{0,255};
    
    Testee = Component
    elementary
      data: U8 = 42;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


#TODO implement
@fixme
Scenario: use an alias type in the interface
  Given we have a file "testee.rzy" with the content:
    """
    U8 = R{0,255};
    
    Testee = Component
      get: response():U8;

    elementary
      get: response():U8
        return 42;
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  Then I expect the request get() = 42


#TODO this should work
@fixme
Scenario: get the default value for an alias type
  Given we have a file "testee.rzy" with the content:
    """
    U8 = R{0,255};
    
    Testee = Component
    elementary
      data: U8 = default{U8}();
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: Use type templates for aliases
  Given we have a file "testee.rzy" with the content:
    """
    Unsigned{T:Type{Natural}} = T;
    
    Testee = Component
    elementary
      data: Unsigned{R{0,10}} = 0;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: Use value templates for aliases
  Given we have a file "testee.rzy" with the content:
    """
    Unsigned{Max:Natural} = R{0,Max};
    
    Testee = Component
    elementary
      data: Unsigned{10} = 0;
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


