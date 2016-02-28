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

import java.util.Map;

import main.Configuration;
import ast.Designator;
import ast.data.Ast;
import ast.data.Namespace;
import ast.debug.DebugPrinter;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

public class PassRunner {
  public static void process(Configuration opt, String outdir, String debugdir) {
    Namespace aclasses = new Namespace("!");
    KnowledgeBase kb = new KnowledgeBase(aclasses, outdir, debugdir, opt);
    PassGroup passes = PassFactory.makePasses(opt);
    process(passes, new Designator(), new DebugPrinter(aclasses, kb.getDebugDir()), aclasses, kb);
  }

  public static void process(PassGroup group, Designator prefix, DebugPrinter dp, Namespace ast, KnowledgeBase kb) {
    prefix = new Designator(prefix, group.getName());
    for (Pass pass : group.passes) {
      if (pass instanceof PassGroup) {
        process((PassGroup) pass, prefix, dp, ast, kb);
      } else if (pass instanceof PassItem) {
        process((PassItem) pass, prefix, dp, ast, kb);
      } else {
        throw new RuntimeException("not yet implemented: " + pass.getClass().getCanonicalName());
      }
      for (AstPass check : group.checks) {
        check.process(ast, kb);
      }
    }
  }

  public static void process(PassItem item, Designator prefix, DebugPrinter dp, Namespace ast, KnowledgeBase kb) {
    prefix = new Designator(prefix, item.getName());
    item.pass.process(ast, kb);
    dp.print(prefix.toString("."));
  }

  @SuppressWarnings("unused")
  private static void print(Map<Ast, Boolean> writes, Map<Ast, Boolean> reads, Map<Ast, Boolean> outputs, Map<Ast, Boolean> inputs) {
    for (Ast header : writes.keySet()) {
      String rwio = "";
      rwio += reads.get(header) ? "r" : " ";
      rwio += writes.get(header) ? "w" : " ";
      rwio += inputs.get(header) ? "i" : " ";
      rwio += outputs.get(header) ? "o" : " ";
      System.out.print(rwio);
      System.out.print("\t");
      System.out.print(header);
      System.out.println();
    }
  }

}
