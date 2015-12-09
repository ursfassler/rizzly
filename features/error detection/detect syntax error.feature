Feature: Detect syntax error
  As a developer
  I want the compiler to tell where a syntax error was made
  In order to correct it quickly


Scenario: access nonexistent name
  Given we have a file "err1.rzy" with the content:
    """
    Err1 = Component
      out : signal();
      inp : slot();
      
    elementary
      inp : slot()
        out.foo();
      end
      
    end

    """

  When I start rizzly with the file "err1.rzy"
  
  Then I expect an error code
  And stderr should contain "err1.rzy:7:8: Fatal: Name not found: foo"


Scenario: missing template parameter
  Given we have a file "err2.rzy" with the content:
    """
    Foo{Bar:Natural} = Component
    elementary
    end

    Err2 = Component
    composition
      foo: Foo;
    end

    """

  When I start rizzly with the file "err2.rzy"
  
  Then I expect an error code
  And stderr should contain "err2.rzy:7:8: Error: Missing template argument"


Scenario: use of reserved keyword (R) as template argument name
  Given we have a file "err3.rzy" with the content:
    """
    Rec{R:Any} = Record
      a : R;
    end

    Err3 = Component
    elementary
      dat: Rec{Boolean} = [ a := False ];
    end

    """

  When I start rizzly with the file "err3.rzy"
  
  Then I expect an error code
  And stderr should contain "err3.rzy:1:6: Error: Expected name, got keyword R"


Scenario: reference non existing state
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      evt : signal();
    hfsm(A)
      A: state(B)
        B: state(C)
          C: state;
        end
      end
      A to A.C by evt();
    end

    """

  When I start rizzly with the file "testee.rzy"
  
  Then I expect an error code
  And stderr should contain "testee.rzy:9:8: Fatal: Item not found"


@fixme
Scenario: wrong order of array arguments
  Given we have a file "err5.rzy" with the content:
    """
    Err5 = Component
    elementary
      a: Array{R{0,10}, 3} = ( 1, 2, 3 );
    end

    """

  When I start rizzly with the file "err5.rzy"
  
  Then I expect an error code
#  And stderr should contain "err5.rzy:3:11: Error: Expected value, got type"


@fixme
Scenario: array elements have to be initialized
  Given we have a file "err6.rzy" with the content:
    """
    Err6 = Component
      test : slot(x: R{0,10});

    elementary
      buffer  : Array{10, R{0,10}} = ();
      
      test : slot(x: R{0,10})
        buffer[3] := x;
      end
    end

    """

  When I start rizzly with the file "err6.rzy"
  
  Then I expect an error code
#  And stderr should contain "err6.rzy:5:32: Error: need 10 elements, got 0"


