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
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import ast.Designator;

public class ClaOption {
  static public final String extension = ".rzy";

  private String rootPath;
  private Designator rootComp;
  private boolean debugEvent;
  private boolean docOutput;
  private boolean lazyModelCheck;

  public String getRootPath() {
    return rootPath;
  }

  public Designator getRootComp() {
    return rootComp;
  }

  public boolean doDebugEvent() {
    return debugEvent;
  }

  public boolean doLazyModelCheck() {
    return lazyModelCheck;
  }

  public boolean getDocOutput() {
    return docOutput;
  }

  public void init(String rootPath, Designator rootComp, boolean debugEvent, boolean lazyModelCheck) {
    this.rootPath = rootPath;
    this.rootComp = rootComp;
    this.debugEvent = debugEvent;
    this.lazyModelCheck = lazyModelCheck;
  }

  public boolean parse(String[] args) {
    Options opt = new Options();
    opt.addOption("h", "help", false, "show help");
    opt.addOption("p", "rootdir", true, "directory of the project, default is the working directory");
    opt.addOption("r", "rootcomp", true, "path of the root component");
    opt.addOption("i", "input", true, "input file, the root component needs the same name as the file but beginning with an uppercase letter");
    opt.addOption(null, "doc", false, "generate documentation");
    opt.addOption(null, "debugEvent", false, "produce code to get informed whenever a event is sent or received");
    opt.addOption(null, "lazyModelCheck", false, "Do not check constraints of model. Very insecure!");

    CommandLineParser parser = new PosixParser();
    try {
      CommandLine cmd = parser.parse(opt, args);

      if (cmd.hasOption("h")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("rizzly", opt);
        return false;
      }

      if (cmd.hasOption("r") && cmd.hasOption("i")) {
        System.err.println("Can not use option 'r' with option 'i'");
        return false;
      }

      if (cmd.hasOption("p") && !cmd.hasOption("r")) {
        System.err.println("Option 'p' needs option 'r'");
        return false;
      }

      String rootdir;
      Designator rootcomp;

      if (cmd.hasOption("r")) {
        rootdir = cmd.getOptionValue("p", "." + File.separator);
        String rootpath = cmd.getOptionValue("r");
        ArrayList<String> nam = new ArrayList<String>();
        for (String p : rootpath.split("\\.")) {
          nam.add(p);
        }
        assert (nam.size() > 1);
        rootcomp = new Designator(nam);
      } else if (cmd.hasOption("i")) {
        rootdir = cmd.getOptionValue("i");
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
        rootcomp = new Designator(filename, compname);
      } else {
        System.err.println("Need option 'r' or 'i'");
        return false;
      }

      File file = new File(rootdir);
      file = file.getAbsoluteFile();
      rootdir = file.toString() + File.separator;

      rootPath = rootdir;
      rootComp = rootcomp;

      debugEvent = cmd.hasOption("debugEvent");
      lazyModelCheck = cmd.hasOption("lazyModelCheck");
      docOutput = cmd.hasOption("doc");

      return true;
    } catch (ParseException e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("rizzly", opt);
      return false;
    }
  }

}
