Feature: Use hierarchical finite state machines in  Rizzly
  As a developer
  I want to use hfsm
  In order to handle events easily


Scenario: very big state machine
  Given we have a file "hfsm1.rzy" with the content:
    """
    Hfsm1 = Component
      tick : slot();
      evt : signal( value: R{0,255} );

    hfsm(A)
      AA to B.BB by tick() do
        evt( 1 );
      end
      AA.BB to B by tick() do
        evt( 2 );
      end
      
      A : state(AA)
        entry
          evt( 10 );
        end
        exit
          evt( 19 );
        end
        
        AA : state(AAA)
          entry
            evt( 20 );
          end
          exit
            evt( 29 );
          end
          
          AAA : state
            entry
              evt( 30 );
            end
            exit
              evt( 39 );
            end
          end
          
          AA to BB by tick() do
            evt( 3 );
          end
      
          BB : state
            entry
              evt( 40 );
            end
            exit
              evt( 49 );
            end
          end
        end
      end

      B : state(BB)
        entry
          evt( 50 );
        end
        exit
          evt( 59 );
        end

        BB : state(BBB)
          entry
            evt( 60 );
          end
          exit
            evt( 69 );
          end
          
          BBB : state
            entry
              evt( 70 );
            end
            exit
              evt( 79 );
            end
          end
        end
      end
      
      BBB to AAA by tick() do
        evt( 3 );
      end
    end
    
    """

  When I succesfully compile "hfsm1.rzy" with rizzly
  And fully compile everything
  
  When I initialize it
  Then I expect an event evt(10)
  And I expect an event evt(20)
  And I expect an event evt(30)
  And I expect no more events

  When I send an event tick()
  Then I expect an event evt(39)
  Then I expect an event evt(29)
  Then I expect an event evt(19)
  Then I expect an event evt(1)
  Then I expect an event evt(50)
  Then I expect an event evt(60)
  Then I expect an event evt(70)
  And I expect no more events
  
  When I send an event tick()
  Then I expect an event evt(79)
  Then I expect an event evt(69)
  Then I expect an event evt(59)
  Then I expect an event evt(3)
  Then I expect an event evt(10)
  Then I expect an event evt(20)
  Then I expect an event evt(30)
  And I expect no more events
  
  When I send an event tick()
  Then I expect an event evt(39)
  Then I expect an event evt(29)
  Then I expect an event evt(19)
  Then I expect an event evt(1)
  Then I expect an event evt(50)
  Then I expect an event evt(60)
  Then I expect an event evt(70)
  And I expect no more events
  
  When I deinitialize it
  Then I expect an event evt(79)
  And I expect an event evt(69)
  And I expect an event evt(59)
  And I expect no more events


Scenario: default responses
  Given we have a file "response.rzy" with the content:
    """
    Response = Component
      tick : slot();
      read : response():R{0,100};

    hfsm( A )
      A to B by tick();
      B to C by tick();
      C to A by tick();

      A : state;
        
      B : state
        read : response():R{0,100}
          return 42;
        end
      end

      C : state;
      
      read : response():R{0,100}
        return 23;
      end
    end
    
    """

  When I succesfully compile "response.rzy" with rizzly
  And fully compile everything
  
  When I initialize it
  Then I expect the request read() = 23

  When I send an event tick()
  Then I expect the request read() = 42

  When I send an event tick()
  Then I expect the request read() = 23


Scenario: use first matching response
  Given we have a file "response3.rzy" with the content:
    """
    Response3 = Component
      read : response():R{0,100};
      
    hfsm( A )
      read : response():R{0,100}
        return 57;
      end

      A : state
        read : response():R{0,100}     // this response is never used
          return 42;
        end
      end
    end    
    
    """

  When I succesfully compile "response3.rzy" with rizzly
  And fully compile everything
  
  When I initialize it
  Then I expect the request read() = 57


Scenario: no default response is needed when all sub-states have the response defined
  Given we have a file "response4.rzy" with the content:
    """
    Response4 = Component
      tick : slot();
      read : response():R{0,100};
        
    hfsm( A )
      A to B by tick();

      A : state
        read : response():R{0,100}
          return 1;
        end
      end
        
      B : state
        read : response():R{0,100}
          return 2;
        end
      end
    end
    
    """

  When I succesfully compile "response4.rzy" with rizzly
  And fully compile everything
  
  When I initialize it
  Then I expect the request read() = 1

  When I send an event tick()
  Then I expect the request read() = 2


Scenario: states can have the same name as states with a different parent
  Given we have a file "stateNames1.rzy" with the content:
    """
    StateNames1 = Component

    hfsm(A)
      A : state(A)
        A : state(A)
          A : state;
          B : state;
          C : state;
        end
        B : state(A)
          A : state;
          B : state;
          C : state;
        end
      end
    end
    
    """

  When I start rizzly with the file "stateNames1.rzy"
  
  Then I expect no error


Scenario: I can access states by fully specify their name
  Given we have a file "stateNames2.rzy" with the content:
    """
    StateNames2 = Component
      tick : slot();
      evt : signal(value: R{0,255});

    hfsm(A)
      A : state(A)
        A : state(A)
          A : state
            entry
              evt(1);
            end
          end
        end
        B : state(A)
          A : state
            entry
              evt(2);
            end
          end
        end
      end
      
      A.A to B.A by tick();
    end
    
    """

  When I succesfully compile "stateNames2.rzy" with rizzly
  And fully compile everything
  
  When I initialize it
  Then I expect an event evt(1)
  And I expect no more events

  When I send an event tick()
  Then I expect an event evt(2)
  And I expect no more events


Scenario: A transition can not go to an outer state
  Given we have a file "transDist1.rzy" with the content:
    """
    TransDist1 = Component
      tick : slot();
        
    hfsm(A)
      A : state(AA)
        AA : state
          AA to A by tick();
        end
      end
    end
    
    """

  When I start rizzly with the file "transDist1.rzy"
  
  Then I expect an error code
  And stderr should contain "transDist1.rzy:7:13: Error: State not found: A"


Scenario: A transition can not come from an outer state
  Given we have a file "transDist1.rzy" with the content:
    """
    TransDist1 = Component
      tick : slot();
        
    hfsm(A)
      A : state(AA)
        AA : state
          A to AA by tick();
        end
      end
    end
    
    """

  When I start rizzly with the file "transDist1.rzy"
  
  Then I expect an error code
  And stderr should contain "transDist1.rzy:7:7: Error: State not found: A"


Scenario: The first matching transition is taken
  Given we have a file "transOrder.rzy" with the content:
    """
    TransOrder = Component
      inp : slot( val: R{0,63} );
      out : signal( val: R{0,63} );
        
    hfsm(A)
      A to A by inp( val: R{0,63} ) if val <= 0 do
        out( 0 );
      end
      
      A : state
        A to A by inp( val: R{0,63} ) if val <= 1 do
          out( 1 );
        end
      end

      A to A by inp( val: R{0,63} ) if val <= 2 do
        out( 2 );
      end
    end
    
    """

  When I succesfully compile "transOrder.rzy" with rizzly
  And fully compile everything
  
  When I initialize it
  Then I expect no more events

  When I send an event inp(0)
  Then I expect an event out(0)
  And I expect no more events

  When I send an event inp(1)
  Then I expect an event out(1)
  And I expect no more events

  When I send an event inp(2)
  Then I expect an event out(2)
  And I expect no more events


Scenario: The transition guard is evaluated in the source state
  Given we have a file "guardScope.rzy" with the content:
    """
    GuardScope = Component
      tick : slot();
      aToA : signal();
      aToB : signal();
      bToB : signal();
      bToA : signal();
      
    hfsm( A )
      a : R{0,0} = 0;     // this variable is never read since A.a and B.a shadows it
          
      A to B by tick() if a = 0 do   // guard is evaluated in the scope of the source state
        aToB();
      end
      
      B to A by tick() if a = 0 do
        bToA();
      end
      
      A : state
        a : R{0,2} = 2;
            
        A to A by tick() if a > 0 do
          a := R{0,2}( a - 1 );
          aToA();
        end
      end
          
      B : state
        a : R{0,1} = 1;
            
        B to B by tick() if a > 0 do
          a := R{0,1}( a - 1 );
          bToB();
        end
      end
    end
    
    """

  When I succesfully compile "guardScope.rzy" with rizzly
  And fully compile everything
  
  When I initialize it
  Then I expect no more events

  When I send an event tick()
  Then I expect an event aToA()

  When I send an event tick()
  Then I expect an event aToA()

  When I send an event tick()
  Then I expect an event aToB()

  When I send an event tick()
  Then I expect an event bToB()

  When I send an event tick()
  Then I expect an event bToA()
 
  And I expect no more events

@fixme
Scenario: Unreachable states are detected
  Given we have a file "unusedState.rzy" with the content:
    """
    UnusedState = Component
      tick : slot();

    hfsm(A)
      A : state;
      B : state;
      C : state;
      
      A to B by tick();
      B to A by tick();
    end
    
    """

  When I start rizzly with the file "unusedState.rzy"
  
  Then I expect no error
#  And stdout should contain "unusedState.rzy:7:3: Error: Unused state: C"


Scenario: a state can have a variable
  Given we have a file "stateVariable2.rzy" with the content:
    """
    StateVariable2 = Component

    hfsm(A)
      A : state
        a : R{0,255} = 0;
      end
    end
    
    """

  When I start rizzly with the file "stateVariable2.rzy"
  
  Then I expect no error


