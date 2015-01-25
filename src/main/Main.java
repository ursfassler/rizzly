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

import error.RException;
import fun.other.Namespace;
import fun.toevl.FunToEvl;

//TODO set tag when writing to union
//TODO allow writing of tag of union
//TODO check that union/record is fully initialized before passing to other function
//TODO pass records by reference to functions (test case rec2)
//TODO add compiler self tests:
//TODO -- check that no references to old stuff exists (check that parent of every object is in the namespace tree)
//TODO -- do name randomization and compile to see if references go outside
//TODO add compiler switch to select backend (like --backend=ansiC --backend=funHtmlDoc)
//TODO check for zero before division
//TODO check range by user input
//TODO check if event handling is in progress when starting event handling
//TODO allow type declaration in elementary; make sure type is not escaping

public class Main {

  /**
   * @param args
   */
  public static void main(String[] args) {
    ClaOption opt = new ClaOption();
    if (!opt.parse(args)) {
      System.exit(-2);
      return;
    }

    // compile(opt);
    try {
      compile(opt);
    } catch (RException err) {
      System.err.println(err.getMessage());
      System.exit(-1);
    }
    System.exit(0);
  }

  public static String compile(ClaOption opt) {
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

    Namespace fret = MainFun.doFun(opt, debugdir);
    FunToEvl funToAst = new FunToEvl();
    evl.other.Namespace aclasses = (evl.other.Namespace) funToAst.traverse(fret, null);

    MainEvl.doEvl(opt, outdir, debugdir, aclasses);

    return outdir;
  }

}
