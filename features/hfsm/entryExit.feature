Feature: entry and exit functions
  As a developer
  I want to execute code when enter or exit a state
  In order to action state depending listener


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


