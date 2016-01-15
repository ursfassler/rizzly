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

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import ast.Designator;
import ast.ElementInfo;
import error.ErrorType;
import error.RizzlyError;

public class CommandLineParser {
  private final RizzlyOptions options = new RizzlyOptions();
  private final RizzlyError error;
  private static final String extension = ".rzy";

  public CommandLineParser(RizzlyError error) {
    super();
    this.error = error;
  }

  public Configuration parse(String[] args) {
    CommandLine cmd = parseOptions(args);

    if (cmd == null) {
      return null;
    }

    String inputFile = getInputFile(cmd);
    if (!isSane(inputFile, cmd)) {
      return null;
    }

    WritableConfiguration configuration = new WritableConfiguration();
    parsePathAndRoot(inputFile, cmd, configuration);
    configuration.setDebugEvent(cmd.hasOption(options.debugEvent.getLongOpt()));
    configuration.setLazyModelCheck(cmd.hasOption(options.lazyModelCheck.getLongOpt()));
    configuration.setDocOutput(cmd.hasOption(options.documentation.getLongOpt()));
    configuration.setExtension(extension);

    return configuration;
  }

  private String getInputFile(CommandLine cmd) {
    List<String> list = cmd.getArgList();

    String inputFile = null;
    if (list.size() > 0) {
      inputFile = list.get(0);
    }
    return inputFile;
  }

  private void parsePathAndRoot(String inputFile, CommandLine cmd, WritableConfiguration configuration) {
    Designator rootComponent = getRootComponent(inputFile);
    String rootPath = getRootPath(inputFile);

    if (hasOption(cmd, options.component)) {
      rootComponent = stringToDesignator(cmd.getOptionValue(options.component.getLongOpt()));
    }

    configuration.setRootComp(rootComponent);
    configuration.setRootPath(rootPath);
  }

  private boolean isSane(String inputFile, CommandLine cmd) {
    boolean hasComponent = hasOption(cmd, options.component);
    boolean hasRizzlyFile = inputFile != null;  // TODO remove

    if (hasComponent && !hasRizzlyFile) {
      error.err(ErrorType.Error, ElementInfo.NO, "Option '" + options.component.getLongOpt() + "' needs file");
      return false;
    }

    if (!hasRizzlyFile) {
      error.err(ErrorType.Error, ElementInfo.NO, "Need a file");
      printHelp();
      return false;
    }

    return true;
  }

  private boolean hasOption(CommandLine cmd, Option option) {
    String name = option.getLongOpt();
    assert (name != null);
    return cmd.hasOption(name);
  }

  private CommandLine parseOptions(String[] args) {
    PosixParser parser = new PosixParser(); // TODO use GNU parser?
    CommandLine cmd;

    try {
      cmd = parser.parse(options.all, args);
    } catch (ParseException e) {
      printHelp();
      return null;
    }

    if (hasOption(cmd, options.help)) {
      printHelp();
      return null;
    }

    return cmd;
  }

  private void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("rizzly", options.all);
  }

  private Designator getRootComponent(String inputFile) {
    assert (inputFile.endsWith(extension));

    String filename = getFilename(inputFile);

    String namespace = filename.substring(0, filename.length() - extension.length());
    String component = namespace.substring(0, 1).toUpperCase() + namespace.substring(1, namespace.length());

    return new Designator(namespace, component);
  }

  private String getRootPath(String inputFile) {
    String filename = getFilename(inputFile);

    String rootPath = inputFile.substring(0, inputFile.length() - filename.length());
    if (rootPath.equals("")) {
      rootPath = "." + File.separator;
    }

    return rootPath;
  }

  private String getFilename(String inputFile) {
    int idx = inputFile.lastIndexOf(File.separator);
    String filename;
    if (idx < 0) {
      filename = inputFile;
    } else {
      filename = inputFile.substring(idx + File.separator.length(), inputFile.length());
    }
    return filename;
  }

  private Designator stringToDesignator(String value) {
    ArrayList<String> nam = new ArrayList<String>();
    for (String p : value.split("\\.")) {
      nam.add(p);
    }
    return new Designator(nam);
  }
}

class RizzlyOptions {
  public final Option lazyModelCheck = new Option(null, "lazyModelCheck", false, "Do not check constraints of model. Very insecure!");
  public final Option debugEvent = new Option(null, "debugEvent", false, "produce code to get informed whenever a event is sent or received");
  public final Option documentation = new Option(null, "doc", false, "generate documentation");
  public final Option component = new Option("c", "component", true, "the component to instantiate");
  public final Option help = new Option("h", "help", false, "show help");
  public final Options all = new Options();

  RizzlyOptions() {
    all.addOption(help);
    all.addOption(component);
    all.addOption(documentation);
    all.addOption(debugEvent);
    all.addOption(lazyModelCheck);
  }

}
