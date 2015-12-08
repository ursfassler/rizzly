Feature: Use the if then else statement in Rizzly
  As a developer
  I want to branch depending in a predicate
  In order to do different things


Scenario: a if statement can have a then branch
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(a: Boolean);
      branch : signal();
    
    elementary
      inp: slot(a: Boolean)
        if a then
          branch();
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(False)
  Then I expect no more events

  When I send an event inp(True)
  Then I expect an event branch()
  And I expect no more events


Scenario: a if statement can have a then and an else branch
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(a: Boolean);
      branch1 : signal();
      branch2 : signal();
    
    elementary
      inp: slot(a: Boolean)
        if a then
          branch1();
        else
          branch2();
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(False)
  Then I expect an event branch2()
  Then I expect no more events

  When I send an event inp(True)
  Then I expect an event branch1()
  And I expect no more events


#TODO use lower case in error message
@fixme
Scenario: a if statement can not only have an else branch
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(a: Boolean);
    
    elementary
      inp: slot(a: Boolean)
        if a else
        end
      end
    end

    """

  When I start rizzly with the file "testee.rzy"
  
  Then I expect an error code
  And stderr should contain "testee.rzy:6:10: Error: expected THEN got ELSE"


Scenario: a if statement can have an ef branches and the first match is taken
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(a, b, c: Boolean);
      branch1 : signal();
      branch2 : signal();
      branch3 : signal();
    
    elementary
      inp: slot(a, b, c: Boolean)
        if a then
          branch1();
        ef b then
          branch2();
        ef c then
          branch3();
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(False, False, False)
  Then I expect no more events

  When I send an event inp(True, False, False)
  Then I expect an event branch1()
  Then I expect no more events

  When I send an event inp(False, True, False)
  Then I expect an event branch2()
  Then I expect no more events

  When I send an event inp(True, True, False)
  Then I expect an event branch1()
  Then I expect no more events

  When I send an event inp(False, False, True)
  Then I expect an event branch3()
  Then I expect no more events

  When I send an event inp(True, False, True)
  Then I expect an event branch1()
  Then I expect no more events

  When I send an event inp(False, True, True)
  Then I expect an event branch2()
  Then I expect no more events

  When I send an event inp(True, True, True)
  Then I expect an event branch1()
  Then I expect no more events


Scenario: a if statement can have an ef branche and an else
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(a, b: Boolean);
      branch1 : signal();
      branch2 : signal();
      branch3 : signal();
    
    elementary
      inp: slot(a, b: Boolean)
        if a then
          branch1();
        ef b then
          branch2();
        else
          branch3();
        end
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(False, False)
  Then I expect an event branch3()
  Then I expect no more events

  When I send an event inp(True, False)
  Then I expect an event branch1()
  Then I expect no more events

  When I send an event inp(False, True)
  Then I expect an event branch2()
  Then I expect no more events

  When I send an event inp(True, True)
  Then I expect an event branch1()
  Then I expect no more events
  
