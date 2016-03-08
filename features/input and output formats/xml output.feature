Feature: the compiler writes the AST to an xml file
  As a compiler developer or researcher
  I want the compiler to write the AST to an xml file
  In order to study a compiler pass or use the data for further processing

Scenario: write the AST of the simplest rizzly file to an xml file
  Given we have a file "testee.rzy" with the content:
    """

    """

  When I start rizzly with the passes
    | pass                     |
    | rzyreader('.', 'testee') |
    | metadataremover          |
    | xmlwriter('testee.xml')  |
  
  Then I expect no error
  And I expect an xml file "testee.xml" with the content:
    """
    <rizzly>
      <RizzlyFile name="testee" />
    </rizzly>
    
    """

#TODO do not start separate block for entry and exit
#TODO do not write empty sub-items (interface, ...)
Scenario: write the AST of a elementary component to an xml file
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    elementary
    end

    """

  When I start rizzly with the passes
    | pass                     |
    | rzyreader('.', 'testee') |
    | metadataremover          |
    | xmlwriter('testee.xml')  |
    
  Then I expect no error
  And I expect an xml file "testee.xml" with the content:
    """
    <rizzly>
      <RizzlyFile name="testee">
        <Template name="Testee">
          <RawElementary name="Testee">
            <interface/>
            <entry>
              <Block/>
            </entry>
            <exit>
              <Block/>
            </exit>
            <declaration/>
            <instantiation/>
          </RawElementary>
        </Template>
      </RizzlyFile>
    </rizzly>

    """

Scenario: read in a simple xml and output it results in the same xml
  Given we have a file "testee.xml" with the content:
    """
    <rizzly>
      <GlobalConstant name="TheAnswer">
        <Reference>
          <UnlinkedAnchor target="Integer"/>
        </Reference>
        <NumberValue value="42"/>
      </GlobalConstant>
    </rizzly>

    """

  When I start rizzly with the passes
    | pass                     |
    | xmlreader('testee.xml')  |
    | xmlwriter('testee.xml')  |
  
  Then I expect no error
  And I expect an xml file "testee.xml" with the content:
    """
    <rizzly>
      <GlobalConstant name="TheAnswer">
        <Reference>
          <UnlinkedAnchor target="Integer"/>
        </Reference>
        <NumberValue value="42"/>
      </GlobalConstant>
    </rizzly>

    """

Scenario: references are unique
  Given we have a file "input.xml" with the content:
    """
    <rizzly>
      <GlobalConstant name="TheOtherAnswer">
        <Reference>
          <UnlinkedAnchor target="Natural"/>
        </Reference>
        <NumberValue value="42"/>
      </GlobalConstant>
      <GlobalConstant name="TheAnswer">
        <Reference>
          <UnlinkedAnchor target="Integer"/>
        </Reference>
        <NumberValue value="42"/>
      </GlobalConstant>
      <Integer name="Integer"/>
      <Natural name="Natural"/>
    </rizzly>

    """

  When I start rizzly with the passes
    | pass                    |
    | xmlreader('input.xml')  |
    | linker                  |
    | xmlwriter('output.xml') |
  
  Then I expect no error
  And I expect an xml file "output.xml" with the content:
    """
    <rizzly>
      <GlobalConstant name="TheOtherAnswer">
        <Reference>
          <LinkedAnchor link="1"/>
        </Reference>
        <NumberValue value="42"/>
      </GlobalConstant>
      <GlobalConstant name="TheAnswer">
        <Reference>
          <LinkedAnchor link="0"/>
        </Reference>
        <NumberValue value="42"/>
      </GlobalConstant>
      <Integer name="Integer" id="0"/>
      <Natural name="Natural" id="1"/>
    </rizzly>

    """


Scenario: id's are not preserved when reading an xml and writing the same ast
  Given we have a file "input.xml" with the content:
    """
    <rizzly>
      <GlobalConstant name="TheOtherAnswer">
        <Reference>
          <LinkedAnchor link="the natural"/>
        </Reference>
        <NumberValue value="42"/>
      </GlobalConstant>
      <GlobalConstant name="TheAnswer">
        <Reference>
          <LinkedAnchor link="the integer"/>
        </Reference>
        <NumberValue value="42"/>
      </GlobalConstant>
      <Integer name="Integer" id="the integer"/>
      <Natural name="Natural" id="the natural"/>
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
      <GlobalConstant name="TheOtherAnswer">
        <Reference>
          <LinkedAnchor link="1"/>
        </Reference>
        <NumberValue value="42"/>
      </GlobalConstant>
      <GlobalConstant name="TheAnswer">
        <Reference>
          <LinkedAnchor link="0"/>
        </Reference>
        <NumberValue value="42"/>
      </GlobalConstant>
      <Integer name="Integer" id="0"/>
      <Natural name="Natural" id="1"/>
    </rizzly>

    """

