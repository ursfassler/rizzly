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

    if (!isSane(cmd)) {
      return null;
    }

    WritableConfiguration configuration = new WritableConfiguration();
    parsePathAndRoot(cmd, configuration);
    configuration.setDebugEvent(cmd.hasOption(options.debugEvent.getLongOpt()));
    configuration.setLazyModelCheck(cmd.hasOption(options.lazyModelCheck.getLongOpt()));
    configuration.setDocOutput(cmd.hasOption(options.documentation.getLongOpt()));
    configuration.setExtension(extension);

    return configuration;
  }

  private void parsePathAndRoot(CommandLine cmd, WritableConfiguration configuration) {
    if (hasOption(cmd, options.component)) {
      parsePathWithRootComponent(cmd, configuration);
    } else if (hasOption(cmd, options.rizzlyFile)) {
      parseFileWithAutoComponent(cmd, configuration);
    } else {
      error.err(ErrorType.Fatal, ElementInfo.NO, "Need option '" + options.component.getLongOpt() + "' or '" + options.rizzlyFile.getLongOpt() + "'");
    }
  }

  private boolean isSane(CommandLine cmd) {
    boolean hasComponent = hasOption(cmd, options.component);
    boolean hasRizzlyFile = hasOption(cmd, options.rizzlyFile);
    boolean hasPath = hasOption(cmd, options.path);

    if (hasComponent && hasRizzlyFile) {
      error.err(ErrorType.Error, ElementInfo.NO, "Can not use option '" + options.component.getLongOpt() + "' with option '" + options.rizzlyFile.getLongOpt() + "'");
      return false;
    }

    if (hasPath && !hasComponent) {
      error.err(ErrorType.Error, ElementInfo.NO, "Option '" + options.path.getLongOpt() + "' needs option '" + options.component.getLongOpt() + "'");
      return false;
    }

    if (!hasComponent && !hasRizzlyFile) {
      error.err(ErrorType.Error, ElementInfo.NO, "Need option '" + options.component.getLongOpt() + "' or '" + options.rizzlyFile.getLongOpt() + "'");
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
    PosixParser parser = new PosixParser();
    CommandLine cmd;

    try {
      cmd = parser.parse(options.all, args);
    } catch (ParseException e) {
      printHelp(options.all);
      return null;
    }

    if (hasOption(cmd, options.help)) {
      printHelp(options.all);
      return null;
    }

    return cmd;
  }

  private void printHelp(Options opt) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("rizzly", opt);
  }

  private void parseFileWithAutoComponent(CommandLine cmd, WritableConfiguration configuration) {
    String rootdir = cmd.getOptionValue(options.rizzlyFile.getOpt());
    assert (rootdir.endsWith(extension));
    rootdir = rootdir.substring(0, rootdir.length() - extension.length());
    int idx = rootdir.lastIndexOf(File.separator);
    String filename;
    if (idx < 0) {
      filename = rootdir;
      rootdir = "." + File.separator;
    } else {
      idx += File.separator.length();
      filename = rootdir.substring(idx, rootdir.length());
      rootdir = rootdir.substring(0, rootdir.length() - filename.length());
    }
    String compname = filename;
    compname = compname.substring(0, 1).toUpperCase() + compname.substring(1, compname.length());
    configuration.setRootComp(new Designator(filename, compname));
    configuration.setRootPath(rootdir);
  }

  private void parsePathWithRootComponent(CommandLine cmd, WritableConfiguration configuration) {
    String rootdir = cmd.getOptionValue(options.path.getOpt(), "." + File.separator);
    if (!rootdir.endsWith(File.separator)) {
      rootdir += File.separator;
    }
    String rootpath = cmd.getOptionValue(options.component.getOpt());
    ArrayList<String> nam = new ArrayList<String>();
    for (String p : rootpath.split("\\.")) {
      nam.add(p);
    }
    assert (nam.size() > 1);
    configuration.setRootComp(new Designator(nam));
    configuration.setRootPath(rootdir);
  }
}

class RizzlyOptions {
  public final Option lazyModelCheck = new Option(null, "lazyModelCheck", false, "Do not check constraints of model. Very insecure!");
  public final Option debugEvent = new Option(null, "debugEvent", false, "produce code to get informed whenever a event is sent or received");
  public final Option documentation = new Option(null, "doc", false, "generate documentation");
  public final Option rizzlyFile = new Option("i", "input", true, "input file, the root component needs the same name as the file but beginning with an uppercase letter");
  public final Option component = new Option("r", "rootcomp", true, "path of the root component");
  public final Option path = new Option("p", "rootdir", true, "directory of the project, default is the working directory");
  public final Option help = new Option("h", "help", false, "show help");
  public final Options all = new Options();

  RizzlyOptions() {
    all.addOption(help);
    all.addOption(path);
    all.addOption(component);
    all.addOption(rizzlyFile);
    all.addOption(documentation);
    all.addOption(debugEvent);
    all.addOption(lazyModelCheck);
  }

}
