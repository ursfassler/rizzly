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

package evl.pass;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import pass.EvlPass;
import util.Pair;
import util.SimpleGraph;
import util.StreamWriter;

import common.Property;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.copy.Copy;
import evl.expression.reference.BaseRef;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.traverser.CHeaderWriter;
import evl.traverser.DepCollector;
import evl.traverser.FpcHeaderWriter;
import evl.traverser.Renamer;
import evl.type.Type;
import evl.type.base.EnumElement;
import evl.type.composed.NamedElement;
import evl.variable.FuncVariable;

public class HeaderWriter extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    RizzlyProgram prg = evl.getItems(RizzlyProgram.class, false).get(0);
    assert (prg != null);

    evl.other.RizzlyProgram head = makeHeader(prg, kb.getDebugDir());
    Set<String> blacklist = makeBlacklist();
    Renamer.process(head, blacklist);
    List<String> names = new ArrayList<String>();  // TODO get names from code (see MainEvl.addDebug)
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

  private static RizzlyProgram makeHeader(RizzlyProgram prg, String debugdir) {
    RizzlyProgram ret = new RizzlyProgram(prg.getName());
    Set<Evl> anchor = new HashSet<Evl>();
    for (Function func : prg.getFunction()) {
      if (Boolean.TRUE.equals(func.properties().get(Property.Public))) {
        for (FuncVariable arg : func.getParam()) {
          anchor.add(arg.getType().getLink());
        }
        anchor.add(func.getRet().getLink());

        ret.getFunction().add(func);
      }
    }

    Set<Evl> dep = DepCollector.process(anchor);

    for (Evl itr : dep) {
      if (itr instanceof evl.type.Type) {
        ret.getType().add((evl.type.Type) itr);
      } else if (itr instanceof SimpleRef) {
        // element of record type
      } else if (itr instanceof NamedElement) {
        // element of record type
      } else if (itr instanceof EnumElement) {
        // element of enumerator type
      } else {
        RError.err(ErrorType.Fatal, itr.getInfo(), "Object should not be used in header file: " + itr.getClass().getCanonicalName());
      }
    }

    RizzlyProgram cpy = Copy.copy(ret);
    for (Function func : cpy.getFunction()) {
      func.getBody().getStatements().clear();
    }

    toposort(cpy.getType());

    return cpy;
  }

  private static void printCHeader(String outdir, RizzlyProgram cprog, List<String> debugNames, KnowledgeBase kb) {
    String cfilename = outdir + cprog.getName() + ".h";
    CHeaderWriter cwriter = new CHeaderWriter(debugNames, kb);
    try {
      cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void printFpcHeader(String outdir, RizzlyProgram cprog, List<String> debugNames, KnowledgeBase kb) {
    String cfilename = outdir + cprog.getName() + ".pas";
    FpcHeaderWriter cwriter = new FpcHeaderWriter(debugNames, kb);
    try {
      cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void toposort(List<evl.type.Type> list) {
    SimpleGraph<evl.type.Type> g = new SimpleGraph<evl.type.Type>(list);
    for (evl.type.Type u : list) {
      Set<evl.type.Type> vs = getDirectUsedTypes(u);
      for (evl.type.Type v : vs) {
        g.addEdge(u, v);
      }
    }

    ArrayList<evl.type.Type> old = new ArrayList<evl.type.Type>(list);
    int size = list.size();
    list.clear();
    LinkedList<evl.type.Type> nlist = new LinkedList<evl.type.Type>();
    TopologicalOrderIterator<evl.type.Type, Pair<evl.type.Type, evl.type.Type>> itr = new TopologicalOrderIterator<evl.type.Type, Pair<evl.type.Type, evl.type.Type>>(g);
    while (itr.hasNext()) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<evl.type.Type> diff = new ArrayList<evl.type.Type>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert (size == list.size());
  }

  private static Set<evl.type.Type> getDirectUsedTypes(evl.type.Type u) {
    DefTraverser<Void, Set<evl.type.Type>> getter = new DefTraverser<Void, Set<evl.type.Type>>() {

      @Override
      protected Void visitBaseRef(BaseRef obj, Set<Type> param) {
        if (obj.getLink() instanceof Type) {
          param.add((Type) obj.getLink());
        }
        return super.visitBaseRef(obj, param);
      }
    };
    Set<evl.type.Type> vs = new HashSet<evl.type.Type>();
    getter.traverse(u, vs);
    return vs;
  }

}
