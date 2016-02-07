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

import main.pass.PassRunner;
import error.RError;
import error.RException;

public class Main {

  /**
   * @param args
   */
  public static void main(String[] args) {
    CommandLineParser parser = new CommandLineParser(RError.instance());
    Configuration configuration = parser.parse(args);
    if (configuration == null) {
      System.exit(-2);
      return;
    }

    // compile(opt);
    try {
      compile(configuration);
    } catch (RException err) {
      System.err.println(err.getMessage());
      System.exit(-1);
    }
    System.exit(0);
  }

  public static String compile(Configuration opt) {
    String debugdir;
    String outdir;
    String docdir;
    {
      debugdir = opt.getRootPath() + "debug" + File.separator;
      outdir = opt.getRootPath() + "output" + File.separator;
      docdir = opt.getRootPath() + "doc" + File.separator;
      (new File(debugdir)).mkdirs();
      (new File(outdir)).mkdirs();
      (new File(docdir)).mkdirs();
    }

    PassRunner.process(opt, outdir, debugdir);

    return outdir;
  }

}
