Feature: Query is not allowed to change state
  As a developer
  I want the compiler to check that queries do not change state
  In order to detect errors early

Scenario: response is not allowed to output
  Given we have a file "err1.rzy" with the content:
    """
    Err1 = Component
      event : signal();
      what : response():R{0,100};
        
    elementary
      what : response():R{0,100}
        event();
        return 0;
      end
      
    end

    """

  When I start rizzly with the file "err1.rzy"
  
  Then I expect an error code
  And stderr should contain "response (what) is not allowed to change state"


Scenario: response is not allowed to write state
  Given we have a file "err2.rzy" with the content:
    """
    Err2 = Component
      what : response():R{0,100};

    elementary
      a : R{0,100} = 0;
      
      what : response():R{0,100}
        a := 42;
        return 0;
      end
    end

    """

  When I start rizzly with the file "err2.rzy"
  
  Then I expect an error code
  And stderr should contain "response (what) is not allowed to change state"


Scenario: response is not allowed to write state
  Given we have a file "err3.rzy" with the content:
    """
    Err3 = Component
      what : response():R{0,100};
      event : signal();
      
    hfsm(A)
      A : state
        what : response():R{0,100}
          event();
          return 0;
        end
      end
    end

    """

  When I start rizzly with the file "err3.rzy"
  
  Then I expect an error code
  And stderr should contain "response (what) is not allowed to change state"


Scenario: response is not allowed to write state
  Given we have a file "err4.rzy" with the content:
    """
    Err4 = Component
      what : response():R{0,100};

    hfsm(A)
      a : R{0,100} = 0;
      
      A : state
        what : response():R{0,100}
          a := 42;
          return 0;
        end
      end
    end

    """

  When I start rizzly with the file "err4.rzy"
  
  Then I expect an error code
  And stderr should contain "response (what) is not allowed to change state"


Scenario: response is not allowed to output
  Given we have a file "err5.rzy" with the content:
    """
    Err5 = Component
      event : signal();
      what : response():R{0,100};

    elementary
      bar = procedure()
        event();
      end
      
      foo = procedure()
        bar();
      end
      
      what : response():R{0,100}
        foo();
        return 0;
      end
      
    end

    """

  When I start rizzly with the file "err5.rzy"
  
  Then I expect an error code
  And stderr should contain "response (what) is not allowed to change state"


Scenario: response is not allowed to write state
  Given we have a file "err6.rzy" with the content:
    """
    Err6 = Component
      what : response():R{0,100};

    elementary
      a : R{0,100} = 0;
      
      bar = procedure()
        a := 42;
      end
      
      foo = procedure()
        bar();
      end
      
      what : response():R{0,100}
        foo();
        return a;
      end

    end

    """

  When I start rizzly with the file "err6.rzy"
  
  Then I expect an error code
  And stderr should contain "response (what) is not allowed to change state"


Scenario: function is not allowed to output
  Given we have a file "err7.rzy" with the content:
    """
    Err7 = Component
      inp : slot();
      out : signal();

    elementary
      inp : slot()
        foo();
      end
      
      foo = function():Boolean
        out();
        return True;
      end
    end

    """

  When I start rizzly with the file "err7.rzy"
  
  Then I expect an error code
  And stderr should contain "function (foo{}) is not allowed to change state"


Scenario: function is not allowed to output
  Given we have a file "err8.rzy" with the content:
    """
    Err8 = Component
      event : slot();
      
    elementary
      a : R{0,100} = 0;
      
      event : slot()
        foo();
      end

      foo = function():Boolean
        a := 42;
        return a > 10;
      end
    end

    """

  When I start rizzly with the file "err8.rzy"
  
  Then I expect an error code
  And stderr should contain "function (foo{}) is not allowed to change state"

