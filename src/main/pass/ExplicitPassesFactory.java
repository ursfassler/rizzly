/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package main.pass;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ast.meta.MetaListImplementation;
import ast.pass.AstPass;
import ast.pass.input.xml.XmlParserPass;
import ast.pass.linker.Linker;
import error.ErrorType;
import error.RizzlyError;

public class ExplicitPassesFactory {
  final private PassArgumentParser argumentParser;
  final private RizzlyError error;

  final private Map<String, SinglePassFactory> factories = new HashMap<String, SinglePassFactory>();

  public ExplicitPassesFactory(PassArgumentParser argumentParser, RizzlyError error) {
    this.argumentParser = argumentParser;
    this.error = error;

    factories.put("linker", new LinkerFactory());
    factories.put("xmlreader", new XmlReaderFactory());
  }

  public AstPass produce(String call) {
    LinkedList<String> arguments = argumentParser.parse(call);
    if (arguments.isEmpty()) {
      error.err(ErrorType.Error, "could not parse pass definition: " + call, new MetaListImplementation());
      throw new RuntimeException();
    }
    String passName = arguments.pop();

    if (factories.containsKey(passName)) {
      return createPass(passName, arguments);
    } else {
      error.err(ErrorType.Error, "pass not found: " + passName, new MetaListImplementation());
      throw new RuntimeException();
    }
  }

  private AstPass createPass(String passName, LinkedList<String> arguments) {
    SinglePassFactory factory = factories.get(passName);
    verify(factory, arguments);
    return factory.create(arguments);
  }

  private void verify(SinglePassFactory factory, LinkedList<String> arguments) {
    if (factory.expectedArgumentCount() != arguments.size()) {
      error.err(ErrorType.Error, "pass xmlreader expected " + factory.expectedArgumentCount() + " argument but " + arguments.size() + " provided", new MetaListImplementation());
      throw new RuntimeException();
    }
  }

}

interface SinglePassFactory {
  public AstPass create(List<String> arguments);

  public int expectedArgumentCount();
}

class XmlReaderFactory implements SinglePassFactory {

  @Override
  public AstPass create(List<String> arguments) {
    return new XmlParserPass(arguments.get(0));
  }

  @Override
  public int expectedArgumentCount() {
    return 1;
  }
}

class LinkerFactory implements SinglePassFactory {
  @Override
  public AstPass create(List<String> arguments) {
    return new Linker();
  }

  @Override
  public int expectedArgumentCount() {
    return 0;
  }
}
