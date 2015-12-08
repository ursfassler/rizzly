Feature: for loop
  As a developer
  I want to use a for loop
  In order to iterate over lists or ranges


Scenario: loop over a fixed range
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tick: slot();
      tock: signal(x: R{0,100});
    
    elementary
      tick: slot()
        for i in R{10,13} do
          tock(i);
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event tick()
  Then I expect an event tock(10)
  Then I expect an event tock(11)
  Then I expect an event tock(12)
  Then I expect an event tock(13)
  And I expect no more events

