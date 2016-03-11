Feature: the compiler reads the AST from an xml file
  As a compiler developer or researcher
  I want the compiler to read the AST from an xml file
  In order to study a compiler pass or use a backend for generation

Scenario: read a simple AST from an xml file
  Given we have a file "input.xml" with the content:
    """
    <rizzly>
      <RizzlyFile name="testee" />
    </rizzly>
    
    """

  When I start rizzly with the passes
    | pass                    |
    | xmlreader('input.xml')  |
    | xmlwriter('output.xml') |
  
  Then I expect no error
  And I expect an xml file "output.xml" with the content:
    """
    <rizzly>
      <RizzlyFile name="testee" />
    </rizzly>
    
    """


Scenario: unknown xml namespaces are ignored
  Given we have a file "input.xml" with the content:
    """
    <rizzly xmlns:test="http://www.bitzgi.ch/2016/rizzly/test1234">
      <test:Test />
      <RizzlyFile name="testee" />
    </rizzly>

    """

  When I start rizzly with the passes
    | pass                    |
    | xmlreader('input.xml')  |

  Then I expect no error


Scenario: read SourcePosition from xml
  Given we have a file "input.xml" with the content:
    """
    <rizzly xmlns:meta="http://www.bitzgi.ch/2016/rizzly/test/meta">
      <GlobalConstant name="TheConstant">
        <meta:SourcePosition filename="definition.rzy" line="3" row="5"/>
        <Reference>
          <UnlinkedAnchor target="nothing"/>
        </Reference>
        <NumberValue value="0"/>
      </GlobalConstant>
    </rizzly>

    """

  When I start rizzly with the passes
    | pass                    |
    | xmlreader('input.xml')  |
    | xmlwriter('output.xml') |
  
  Then I expect no error
  And I expect an xml file "output.xml" with the content:
    """
    <rizzly xmlns:meta="http://www.bitzgi.ch/2016/rizzly/test/meta">
      <GlobalConstant name="TheConstant">
        <meta:SourcePosition filename="definition.rzy" line="3" row="5"/>
        <Reference>
          <UnlinkedAnchor target="nothing"/>
        </Reference>
        <NumberValue value="0"/>
      </GlobalConstant>
    </rizzly>
    
    """

