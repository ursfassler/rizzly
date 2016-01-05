Feature: Process to test rizzly code
  As a rizzly developer
  I want the acceptance test framework to test compiled code behaviour
  In order to show the compiler features


Scenario: Compile a rizzly file and support code, test behaviour
  Given we have a file "test.rzy" with the content:
    """
    Test = Component
      click : slot();
      on : signal();
      off : signal();
      clickCount : signal(count: R{0,100});
      
    hfsm(Off)
      count : R{0,100} = 0;
    
      Off : state
        entry
          off();
        end
      end
      
      On : state
        entry
          on();
          if count < 100 then
            count := R{0,100}(count + 1);
          else
            count := 0;
          end
          clickCount(count);
        end
      end
      
      Off to On by click();
      On to Off by click();
      
    end

    """

  When I start rizzly with the file "test.rzy"
  And fully compile everything
  And I initialize it
  And I send an event click()
  And I send an event click()
  And I send an event click()
  
  Then I expect an event off()
  And I expect an event on()
  And I expect an event clickCount(1)
  And I expect an event off()
  And I expect an event on()
  And I expect an event clickCount(2)
  And I expect no more events

