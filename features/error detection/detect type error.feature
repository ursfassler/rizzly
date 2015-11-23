Feature: Detect type error
  As a developer
  I want the compiler to tell where a type error was made
  In order to correct it quickly


Scenario: assign enum to range
  Given we have a file "err1.rzy" with the content:
    """
    Weekday = Enum
      Monday;
      Tuesday;
      Wednesday;
      Thursday;
      Friday;
      Saturday;
      Sunday;
    end

    Err1 = Component
      foo : slot( x: Weekday );

    elementary
      foo : slot( x: Weekday )
        z : R{0,10} = x;
      end
    end

    """

  When I start rizzly with the file "err1.rzy"
  
  Then I expect an error code
  And stderr should contain "err1.rzy:16:5: Error: Data type to big or incompatible in assignment: R{0,10} := Weekday{}"


Scenario: assign range to enum
  Given we have a file "err2.rzy" with the content:
    """
    Weekday = Enum
      Monday;
      Tuesday;
      Wednesday;
      Thursday;
      Friday;
      Saturday;
      Sunday;
    end

    Err2 = Component
      foo : slot( x: R{0,10} );

    elementary
      foo : slot( x: R{0,10} )
        z : Weekday = x;
      end
    end

    """

  When I start rizzly with the file "err2.rzy"
  
  Then I expect an error code
  And stderr should contain "err2.rzy:16:5: Error: Data type to big or incompatible in assignment: Weekday{} := R{0,10}"


Scenario: arithmetic operations are not allowed on enums
  Given we have a file "err3.rzy" with the content:
    """
    Weekday = Enum
      Monday;
      Tuesday;
      Wednesday;
      Thursday;
      Friday;
      Saturday;
      Sunday;
    end

    Err3 = Component
      foo : slot( x: Weekday );

    elementary
      foo : slot( x: Weekday )
        z : Weekday = x + Weekday.Tuesday;
      end
    end

    """

  When I start rizzly with the file "err3.rzy"
  
  Then I expect an error code
  And stderr should contain "err3.rzy:16:19: Fatal: Expected range type, got Weekday{}"


Scenario: no implicit conversion from enum to range
  Given we have a file "err4.rzy" with the content:
    """
    Weekday = Enum
      Monday;
      Tuesday;
      Wednesday;
      Thursday;
      Friday;
      Saturday;
      Sunday;
    end

    Err4 = Component
      foo : slot( x: Weekday );

    elementary
      foo : slot( x: Weekday )
        bar( x );
      end

      bar = procedure( x: R{0,10} )
      end

    end

    """

  When I start rizzly with the file "err4.rzy"
  
  Then I expect an error code
  And stderr should contain "err4.rzy:16:10: Error: Data type to big or incompatible (argument 1, R{0,10} := Weekday{})"


Scenario: procedure has no return value
  Given we have a file "err5.rzy" with the content:
    """
    Err5 = Component
      out : signal();
      inp : slot();

    elementary
      a : Array{10,R{0,10}} = ( 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 );
      
      inp : slot()
        return a[out()];
      end
      
    end

    """

  When I start rizzly with the file "err5.rzy"
  
  Then I expect an error code
  And stderr should contain "err5.rzy:9:13: Error: array index type is R{0,9}, got Void"


Scenario: Wrong number of initializer elements
  Given we have a file "err6.rzy" with the content:
    """
    Err6 = Component
    elementary
      a : Array{8,R{0,10}} = (0,0);
    end

    """

  When I start rizzly with the file "err6.rzy"
  
  Then I expect an error code
  And stderr should contain "err6.rzy:3:26: Error: Expected 8 elements, got 2"


Scenario: Wrong array index type
  Given we have a file "err7.rzy" with the content:
    """
    Err7 = Component
      get: response( idx: R{0,3} ):R{0,10};
    elementary
      a : Array{3,R{0,10}} = (0, 0, 0);
      
      get: response( idx: R{0,3} ):R{0,10}
        return a[idx];
      end
    end

    """

  When I start rizzly with the file "err7.rzy"
  
  Then I expect an error code
  And stderr should contain "err7.rzy:7:13: Error: array index type is R{0,2}, got R{0,3}"

