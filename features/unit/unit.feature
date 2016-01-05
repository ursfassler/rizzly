Feature: Use units to split up the program
  As a developer
  I want to split my application into separate units
  In order to make the code cleaner and group related stuff together


#TODO allow top unit without component (i.e. instanciation)
@fixme
Scenario: The simples unit does have nothing in it
  Given we have a file "testee.rzy" with the content:
    """
    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


#TODO remove component
Scenario: declare a constant in a unit
  Given we have a file "testee.rzy" with the content:
    """
    Answer : const R{0,99} = 42;

    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


#TODO remove component
Scenario: declare a constant with type inference in a unit
  Given we have a file "testee.rzy" with the content:
    """
    Answer : const = 42;

    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


#TODO remove component
Scenario: It is not allowed to declare a variable in a unit
  Given we have a file "testee.rzy" with the content:
    """
    Answer : R{0,99} = 42;

    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:1:11: Error: expected CONST got IDENTIFIER(R)"


#TODO remove component
Scenario: it is not allowed to declare a procedure in a unit (because it does not make any sense)
  Given we have a file "testee.rzy" with the content:
    """
    tuwas = procedure(a: R{0,100})
    end

    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect an error code
  And stderr should contain "testee.rzy:1:10: Fatal: Expected record, union or type reference"

