Feature: wrong interface connection detection
  As a developer
  I want the compiler to check the interface connections
  In order to detect errors early

Scenario: connect from sub-component input to output
  Given we have a file "err2.rzy" with the content:
    """
    Sub = Component
      inp : slot();
      out : signal();
        
    composition
      inp -> out;
    end

    Err2 = Component
      inp : slot();
      out : signal();

    composition
      a : Sub;
        
      inp -> a.inp;   // prevent error throwing
      a.out -> out;   // prevent error throwing
      
      a.inp -> out;   // ERROR connect sub-input to output
    end

    """

  When I start rizzly with the file "err2.rzy"
  
  Then I expect an error code
  And stderr should contain "can not connect from input"


Scenario: responses have to be connected
  Given we have a file "err3.rzy" with the content:
    """
    Err3 = Component
      funcA : response():Boolean;
    composition
    end

    """

  When I start rizzly with the file "err3.rzy"
  
  Then I expect an error code
  And stderr should contain "Interface funcA not connected"


Scenario: can not connect to non-existing slot
  Given we have a file "err4.rzy" with the content:
    """
    Sub = Component
      funcA : signal();
      funcB : signal();
    elementary
    end

    Err4 = Component
      funcA : signal();
      funcB : signal();
      funcC : signal();
        
    composition
      a : Sub;

      a.funcA -> funcA;
      a.funcB -> funcB;
      a.funcC -> funcC;
    end

    """

  When I start rizzly with the file "err4.rzy"
  
  Then I expect an error code
  And stderr should contain "Interface not found: a.funcC"


Scenario: Query of sub-component has to be connected
  Given we have a file "err7.rzy" with the content:
    """
    Sub = Component
      funcA : query():Boolean;
    elementary
    end

    Err7 = Component
    composition
      a : Sub;
    end

    """

  When I start rizzly with the file "err7.rzy"
  
  Then I expect an error code
  And stderr should contain "Interface a.funcA not connected"


Scenario: Event for transition has to be declared
  Given we have a file "errHfsm2.rzy" with the content:
    """
    ErrHfsm2 = Component
      funcA : slot();
      funcB : slot();

    hfsm(One)
      One : state;
      One to One by funcA();
      One to One by funcB();
      One to One by funcC();
    end

    """

  When I start rizzly with the file "errHfsm2.rzy"
  
  Then I expect an error code
  And stderr should contain "Name not found: funcC"


Scenario: Transition can not be triggered by procedure
  Given we have a file "errHfsm3.rzy" with the content:
    """
    ErrHfsm3 = Component
      funcA : slot();
      funcB : slot();

    hfsm(One)
      One : state;

      funcC = procedure()
      end
      
      One to One by funcA();
      One to One by funcB();
      One to One by funcC();
    end

    """

  When I start rizzly with the file "errHfsm3.rzy"
  
  Then I expect an error code
  And stderr should contain "transition can only be triggered by slot"


