Feature: Transitions guards
  As a developer
  I want to use transition guards
  In order to exactly define wich transition is taken


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

      A : state(A)
        A : state;
      
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


#TODO generates C code that does not compile
@fixme
Scenario: The transition guard is evaluated in the source state
  Given we have a file "guardScope.rzy" with the content:
    """
    GuardScope = Component
      tick : slot();
      aToA : signal();
      aToB : signal();
      bToB : signal();
      bToA : signal();

    hfsm(A)
      a : R{0,0} = 0;     // this variable is never read since A.a and B.a shadows it

      A to B by tick() if a = 0 do   // guard is evaluated in the scope of the source state
        aToB();
      end

      B to A by tick() if a = 0 do
        bToA();
      end

      A : state(A)
        a : R{0,2} = 2;

        AA : state;

        A to A by tick() if a > 0 do
          a := R{0,2}( a - 1 );
          aToA();
        end
      end

      B : state(B)
        a : R{0,1} = 1;

        B : state;

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



