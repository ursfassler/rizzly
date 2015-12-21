Feature: Use elementary as a implementation of components in Rizzly
  As a developer
  I want to implement a component with event handler with code
  In order to do some calculation or similar things when an event comes in


Scenario: The simplest elementary implementation is empty
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: The elementary can have entry code
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    elementary
      entry
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: The entry code is executed when the system is initialized
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tock: signal();
    elementary
      entry
        tock();
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  Then I expect no more events

  When I initialize it
  Then I expect an event tock()
  And I expect no more events


Scenario: The elementary can have exit code
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    elementary
      exit
      end
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error


Scenario: The exit code is executed when the system is deinitialized
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
      tock: signal();
    elementary
      exit
        tock();
      end
    end

    """

  When I succesfully compile "testee.rzy" with rizzly
  And fully compile everything
  And I initialize it
  Then I expect no more events

  When I deinitialize it
  Then I expect an event tock()
  And I expect no more events



