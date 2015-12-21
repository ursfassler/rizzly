Feature: Use the interface to Rizzly with arrays
  As a developer
  I want to use arrays in the interface
  In order to pass and receive lists


#TODO implement
@fixme
Scenario: A slot with an array
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(x: Array{10, R{0,20}});
    composition
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error
  And I expect the fragment "inp(Array_10_R_0_20 const *x)" in the interface description


#TODO implement
@fixme
Scenario: A signal with an array
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      out: signal(x: Array{11, R{3,7}});
    composition
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error
  And I expect the fragment "out(Array_11_R_3_7 const *x)" in the interface description


#TODO implement this in the behave interface generator
@fixme
Scenario: Pass an array to rizzly
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      inp: slot(data: Array{4, R{0,20}});
      out: signal(value: R{0,20});

    elementary
      inp: slot(data: Array{4, R{0,20}})
        out(data[2]);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp([4, 17, 12, 3])
  Then I expect an event out(12)
  And I expect no more events


#TODO implement this in the behave interface generator
@fixme
Scenario: Pass an array from rizzly to the world
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      set: slot(a, b: R{0,20});
      out: signal(data: Array{2, R{0,20}});

    elementary
      set: slot(a, b: R{0,20})
        out([a, b]);
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it

  When I send an event inp(4, 17)
  Then I expect an event out([4, 17])
  And I expect no more events

