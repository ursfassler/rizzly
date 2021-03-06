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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ast.Designator;
import ast.data.variable.Constant;
import ast.meta.MetaListImplementation;
import ast.pass.AstPass;
import ast.pass.input.xml.XmlParserPass;
import ast.pass.linker.Linker;
import ast.pass.others.DefaultVisitorPass;
import ast.pass.others.FileLoader;
import ast.pass.others.InternsAdder;
import ast.pass.output.xml.XmlWriterPass;
import ast.pass.reduction.MetadataRemover;
import ast.repository.query.Referencees.TargetResolver;
import ast.specification.IsClass;
import ast.specification.Specification;
import error.ErrorType;
import error.RError;
import error.RizzlyError;

public class ExplicitPassesFactory {
  final private PassArgumentParser argumentParser;
  final private RizzlyError error;

  final private Map<String, SinglePassFactory> factories = new HashMap<String, SinglePassFactory>();

  public ExplicitPassesFactory(PassArgumentParser argumentParser, RizzlyError error) {
    this.argumentParser = argumentParser;
    this.error = error;

    factories.put("linker", new LinkerFactory());
    factories.put("internsAdder", new InternsAdderFactory());
    factories.put("xmlreader", new XmlReaderFactory());
    factories.put("xmlwriter", new XmlWriterFactory());
    factories.put("rzyreader", new RzyReaderFactory());
    factories.put("metadataremover", new MetadataremoverFactory());
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
    verify(passName, factory, arguments);
    return factory.create(arguments);
  }

  private void verify(String passName, SinglePassFactory factory, LinkedList<String> arguments) {
    if (factory.expectedArgumentCount() != arguments.size()) {
      error.err(ErrorType.Error, "pass " + passName + " expected " + factory.expectedArgumentCount() + " argument but " + arguments.size() + " provided", new MetaListImplementation());
      throw new RuntimeException();
    }
  }
}

interface SinglePassFactory {
  public AstPass create(List<String> arguments);

  public int expectedArgumentCount();
}

class RzyReaderFactory implements SinglePassFactory {

  @Override
  public AstPass create(List<String> arguments) {
    String rootpath = arguments.get(0);
    String[] names = arguments.get(1).split("\\.");
    Designator root = new Designator(Arrays.asList(names));
    return new FileLoader(rootpath, root);
  }

  @Override
  public int expectedArgumentCount() {
    return 2;
  }
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

class XmlWriterFactory implements SinglePassFactory {

  @Override
  public AstPass create(List<String> arguments) {
    return new XmlWriterPass(arguments.get(0));
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

class InternsAdderFactory implements SinglePassFactory {
  @Override
  public AstPass create(List<String> arguments) {
    return new InternsAdder();
  }

  @Override
  public int expectedArgumentCount() {
    return 0;
  }
}

class MetadataremoverFactory implements SinglePassFactory {
  @Override
  public AstPass create(List<String> arguments) {
    return new DefaultVisitorPass(new MetadataRemover());
  }

  @Override
  public int expectedArgumentCount() {
    return 0;
  }
}
