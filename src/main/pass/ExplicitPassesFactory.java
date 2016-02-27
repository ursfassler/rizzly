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

import main.Configuration;
import ast.meta.MetaListImplementation;
import ast.pass.AstPass;
import ast.pass.input.xml.XmlParserPass;
import ast.pass.linker.Linker;
import error.ErrorType;
import error.RizzlyError;

public class ExplicitPassesFactory {
  final private PassArgumentParser argumentParser;
  final private Configuration configuration;
  final private RizzlyError error;

  final private Map<String, SinglePassFactory> factories = new HashMap<String, SinglePassFactory>();

  public ExplicitPassesFactory(PassArgumentParser argumentParser, Configuration configuration, RizzlyError error) {
    this.argumentParser = argumentParser;
    this.configuration = configuration;
    this.error = error;

    factories.put("linker", new SinglePassFactory() {
      @Override
      public AstPass create(List<String> arguments) {
        return new Linker(null);
      }
    });
    factories.put("xmlreader", new SinglePassFactory() {
      @Override
      public AstPass create(List<String> arguments) {
        return new XmlParserPass(null, arguments.get(0));
      }
    });
  }

  public AstPass produce(String call) {
    LinkedList<String> arguments = argumentParser.parse(call);

    String passName = arguments.pop();

    if (factories.containsKey(passName)) {
      return factories.get(passName).create(arguments);
    } else {
      error.err(ErrorType.Error, "pass not found: " + passName, new MetaListImplementation());
      throw new RuntimeException();
    }
  }
}

interface SinglePassFactory {
  public AstPass create(List<String> arguments);
}
