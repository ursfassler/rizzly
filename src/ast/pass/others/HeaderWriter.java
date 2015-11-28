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
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.function.Function;
import ast.data.function.ret.FuncReturn;
import ast.data.reference.Reference;
import ast.data.type.Type;
import ast.data.type.TypeRef;
import ast.data.type.base.EnumElement;
import ast.data.type.composed.NamedElement;
import ast.data.variable.FuncVariable;
import ast.dispatcher.DfsTraverser;
import ast.dispatcher.other.CHeaderWriter;
import ast.dispatcher.other.DepCollector;
import ast.dispatcher.other.FpcHeaderWriter;
import ast.dispatcher.other.Renamer;
import ast.doc.SimpleGraph;
import ast.doc.StreamWriter;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.others.behave.InputWriter;
import ast.pass.others.behave.OutputWriter;
import ast.repository.query.TypeFilter;
import ast.specification.ExternalFunction;
import ast.specification.OrSpec;
import ast.specification.PublicFunction;
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

    printBehaveInput(kb.getOutDir(), head, names, kb);
    printBehaveOutput(kb.getOutDir(), head, names, kb);
    printBehaveMake(kb.getOutDir());
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

    AstList<Function> functions = ast.repository.query.List.select(prg.children, new OrSpec(new PublicFunction(), new ExternalFunction())).castTo(Function.class);
    for (Function func : functions) {
      for (FuncVariable arg : func.param) {
        anchor.add(arg.type.ref);
      }
      anchor.add(func.ret);
    }
    ret.children.addAll(functions);

    Set<Ast> dep = DepCollector.process(anchor);

    for (Ast itr : dep) {
      if (itr instanceof Type) {
        ret.children.add(itr);
      } else if (itr instanceof NamedElement) {
        // element of record type
      } else if (itr instanceof EnumElement) {
        // element of enumerator type
      } else if (itr instanceof FuncReturn) {
      } else if (itr instanceof TypeRef) {
      } else if (itr instanceof Reference) {
      } else {
        RError.err(ErrorType.Fatal, itr.getInfo(), "Object should not be used in header file: " + itr.getClass().getCanonicalName());
      }
    }

    Namespace cpy = Copy.copy(ret);
    for (Function func : TypeFilter.select(cpy.children, Function.class)) {
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

  private void printBehaveInput(String outdir, Namespace head, List<String> debugNames, KnowledgeBase kb) {
    writePyQueue(outdir);
    String cfilename = outdir + head.name + ".py";
    try {
      InputWriter pywriter = new InputWriter(new StreamWriter(new PrintStream(cfilename)));
      pywriter.traverse(head, null);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void printBehaveOutput(String outdir, Namespace head, List<String> debugNames, KnowledgeBase kb) {
    writeCQueue(outdir);
    String cfilename = outdir + head.name + "Cb.cc";
    try {
      OutputWriter ccwriter = new OutputWriter(new StreamWriter(new PrintStream(cfilename)));
      ccwriter.traverse(head, null);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void toposort(AstList<Ast> list) {
    SimpleGraph<Ast> g = new SimpleGraph<Ast>(list);
    for (Ast u : list) {
      Set<Type> vs = getDirectUsedTypes(u);
      for (Type v : vs) {
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

  private static Set<Type> getDirectUsedTypes(Ast u) {
    DfsTraverser<Void, Set<Type>> getter = new DfsTraverser<Void, Set<Type>>() {

      @Override
      protected Void visitReference(Reference obj, Set<Type> param) {
        if (obj.link instanceof Type) {
          param.add((Type) obj.link);
        }
        return super.visitReference(obj, param);
      }
    };
    Set<Type> vs = new HashSet<Type>();
    getter.traverse(u, vs);
    return vs;
  }

  private void writePyQueue(String outdir) {
    try {
      String filename = outdir + "queue.py";
      PrintStream stream = new PrintStream(filename);
      stream.print("from ctypes import *\n" + "\n" + "class Queue:\n" + "  def __init__(self, library):\n" + "    self._inst = CDLL(library)\n" + "\n" + "  def _next(self):\n" + "    size = self._inst.msgSize()\n" + "    p = create_string_buffer(size)\n" + "    self._inst.next(p, sizeof(p))\n" + "    return p.value\n" + "\n" + "  def _canRead(self):\n" + "    return int(self._inst.canRead()) != 0\n");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void writeCQueue(String outdir) {
    try {
      String filename = outdir + "queue.cc";
      PrintStream stream = new PrintStream(filename);
      stream.print("#include \"queue.h\"\n" + "\n" + "#include <queue>\n" + "#include <string.h>\n" + "\n" + "static std::queue<std::string> queue;\n" + "\n" + "extern \"C\"\n" + "{\n" + "\n" + "int canRead()\n" + "{\n" + "  return !queue.empty();\n" + "}\n" + "\n" + "int msgSize()\n" + "{\n" + "  return queue.front().size();\n" + "}\n" + "\n" + "void next(char *msg, int size)\n" + "{\n" + "  strncpy(msg, queue.front().c_str(), size);\n" + "  queue.pop();\n" + "}\n" + "\n" + "}\n" + "\n" + "void push(const std::string &msg)\n" + "{\n" + "  queue.push(msg);\n" + "}\n");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    try {
      String filename = outdir + "queue.h";
      PrintStream stream = new PrintStream(filename);
      stream.print("#ifndef QUEUE_H\n" + "#define QUEUE_H\n" + "\n" + "#include <string>\n" + "\n" + "void push(const std::string &msg);\n" + "\n" + "#endif\n");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void printBehaveMake(String outdir) {
    try {
      String filename = outdir + "Makefile";
      PrintStream stream = new PrintStream(filename);
      stream.print("libinst.so: inst.o instCb.o queue.o\n" + "\tg++ -fPIC -shared $^ -o $@\n" + "\n" + "inst.o: inst.c\n" + "\tgcc -fPIC  -c $^ -o $@\n" + "\n" + "instCb.o: instCb.cc\n" + "\tgcc -std=c++11 -fPIC  -c $^ -o $@\n" + "\n" + "queue.o: queue.cc\n" + "\tgcc -std=c++11 -fPIC  -c $^ -o $@\n" + "\n" + "clean:\n" + "\trm -f *.so *.o *.pyc\n");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

}
