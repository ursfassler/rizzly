Feature: the compiler reads the AST from an xml file
  As a compiler developer or researcher
  I want the compiler to read the AST from an xml file
  In order to study a compiler pass or use a backend for generation

Scenario: read a simple AST from an xml file
  Given we have a file "testee.xml" with the content:
    """
    <rizzly>
      <RizzlyFile name="testee" />
    </rizzly>
    
    """

  When I start rizzly with the file "testee.xml" and the xml backend
  
  Then I expect no error
  And I expect an xml file "testee.xml" with the content:
    """
    <rizzly>
      <RizzlyFile name="testee" />
    </rizzly>
    
    """


