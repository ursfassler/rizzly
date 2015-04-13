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

package ast.pass.others;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import util.Pair;
import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.expression.reference.BaseRef;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.function.ret.FuncReturn;
import ast.data.type.Type;
import ast.data.type.base.EnumElement;
import ast.data.type.composed.NamedElement;
import ast.data.variable.FuncVariable;
import ast.doc.SimpleGraph;
import ast.doc.StreamWriter;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.DefTraverser;
import ast.traverser.other.CHeaderWriter;
import ast.traverser.other.ClassGetter;
import ast.traverser.other.DepCollector;
import ast.traverser.other.FpcHeaderWriter;
import ast.traverser.other.Renamer;
import error.ErrorType;
import error.RError;

public class HeaderWriter extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    Namespace head = makeHeader(ast, kb.getDebugDir());
    Set<String> blacklist = makeBlacklist();
    Renamer.process(head, blacklist);
    List<String> names = new ArrayList<String>(); // TODO get names from code
    // (see MainEvl.addDebug)
    printCHeader(kb.getOutDir(), head, names, kb);
    printFpcHeader(kb.getOutDir(), head, names, kb);
  }

  private static Set<String> makeBlacklist() {
    Set<String> blacklist = new HashSet<String>();
    blacklist.add("if");
    blacklist.add("goto");
    blacklist.add("while");
    blacklist.add("do");
    blacklist.add("byte");
    blacklist.add("word");
    blacklist.add("integer");
    blacklist.add("string");
    return blacklist;
  }

  private static Namespace makeHeader(Namespace prg, String debugdir) {
    Namespace ret = new Namespace(ElementInfo.NO, prg.name);
    Set<Ast> anchor = new HashSet<Ast>();
    for (Function func : ClassGetter.filter(Function.class, prg.children)) {
      if ((func.property == FunctionProperty.Public) || (func.property == FunctionProperty.External)) {
        for (FuncVariable arg : func.param) {
          anchor.add(((SimpleRef<Type>) arg.type).link);
        }
        anchor.add(func.ret);

        ret.children.add(func);
      }
    }

    Set<Ast> dep = DepCollector.process(anchor);

    for (Ast itr : dep) {
      if (itr instanceof ast.data.type.Type) {
        ret.children.add(itr);
      } else if (itr instanceof SimpleRef) {
        // element of record type
      } else if (itr instanceof NamedElement) {
        // element of record type
      } else if (itr instanceof EnumElement) {
        // element of enumerator type
      } else if (itr instanceof FuncReturn) {
      } else {
        RError.err(ErrorType.Fatal, itr.getInfo(), "Object should not be used in header file: " + itr.getClass().getCanonicalName());
      }
    }

    Namespace cpy = Copy.copy(ret);
    for (Function func : ClassGetter.filter(Function.class, cpy.children)) {
      func.body.statements.clear();
    }

    toposort(cpy.children);

    return cpy;
  }

  private static void printCHeader(String outdir, Namespace cprog, List<String> debugNames, KnowledgeBase kb) {
    String cfilename = outdir + cprog.name + ".h";
    CHeaderWriter cwriter = new CHeaderWriter(debugNames, kb);
    try {
      cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void printFpcHeader(String outdir, Namespace cprog, List<String> debugNames, KnowledgeBase kb) {
    String cfilename = outdir + cprog.name + ".pas";
    FpcHeaderWriter cwriter = new FpcHeaderWriter(debugNames, kb);
    try {
      cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void toposort(List<Ast> list) {
    SimpleGraph<Ast> g = new SimpleGraph<Ast>(list);
    for (Ast u : list) {
      Set<ast.data.type.Type> vs = getDirectUsedTypes(u);
      for (ast.data.type.Type v : vs) {
        g.addEdge(u, v);
      }
    }

    ArrayList<Ast> old = new ArrayList<Ast>(list);
    int size = list.size();
    list.clear();
    LinkedList<Ast> nlist = new LinkedList<Ast>();
    TopologicalOrderIterator<Ast, Pair<Ast, Ast>> itr = new TopologicalOrderIterator<Ast, Pair<Ast, Ast>>(g);
    while (itr.hasNext()) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<Ast> diff = new ArrayList<Ast>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert (size == list.size());
  }

  private static Set<ast.data.type.Type> getDirectUsedTypes(Ast u) {
    DefTraverser<Void, Set<ast.data.type.Type>> getter = new DefTraverser<Void, Set<ast.data.type.Type>>() {

      @Override
      protected Void visitBaseRef(BaseRef obj, Set<Type> param) {
        if (obj.link instanceof Type) {
          param.add((Type) obj.link);
        }
        return super.visitBaseRef(obj, param);
      }
    };
    Set<ast.data.type.Type> vs = new HashSet<ast.data.type.Type>();
    getter.traverse(u, vs);
    return vs;
  }

}
