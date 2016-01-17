Feature: the compiler writes the AST to an xml file
  As a compiler developer or researcher
  I want the compiler to write the AST to an xml file
  In order to study a compiler pass or use the data for further processing

Scenario: write the AST of the simplest rizzly file to an xml file
  Given we have a file "testee.rzy" with the content:
    """

    """

  When I start rizzly with the file "testee.rzy" and the xml backend
  
  Then I expect no error
  And I expect an xml file "testee.xml" with the content:
    """
    <rizzly>
      <RizzlyFile name="testee" />
    </rizzly>
    
    """

Scenario: write the AST of a elementary component to an xml file
  Given we have a file "testee.rzy" with the content:
    """
    Testee = Component
    elementary
    end

    """

  When I start rizzly with the file "testee.rzy" and the xml backend
  
  Then I expect no error
  And I expect an xml file "testee.xml" with the content:
    """
    <rizzly>
      <RizzlyFile name="testee">
        <Position filename="./testee.rzy" line="1" row="2"/>
        <Template name="Testee">
          <Position filename="./testee.rzy" line="1" row="2"/>
          <RawElementary name="Testee">
            <Position filename="./testee.rzy" line="2" row="1"/>
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

#TODO make sure x has an unique id and the id is referenced
@fixme
Scenario: references are unique
  Given we have a file "testee.rzy" with the content:
    """
    test = function(x: Boolean):Boolean
      return x;
    end

    """

  When I start rizzly with the file "testee.rzy" and the xml backend
  
  Then I expect no error
  And I expect an xml file "testee.xml" with the content:
    """

    """

