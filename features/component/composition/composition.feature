Feature: Use composition as a implementation of components in Rizzly
  As a developer
  I want to implement a component as composition
  In order to make my design cleaner


Scenario: The simplest composition is empty
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    composition
    end

    """

  When I start rizzly with the file "testee.rzy"

  Then I expect no error



