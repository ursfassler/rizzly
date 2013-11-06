package main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import util.Pair;
import util.SimpleGraph;
import util.StreamWriter;
import cir.function.LibFunction;
import cir.library.CLibrary;
import cir.other.FuncVariable;
import cir.other.Program;
import cir.traverser.BlockReduction;
import cir.traverser.BoolToEnum;
import cir.traverser.CArrayCopy;
import cir.traverser.CWriter;
import cir.traverser.FpcHeaderWriter;
import cir.traverser.RangeReplacer;
import cir.traverser.Renamer;
import cir.traverser.VarDeclToTop;
import cir.type.PointerType;
import cir.type.Type;
import cir.type.TypeRef;
import cir.type.UIntType;
import cir.type.VoidType;

import common.FuncAttr;

public class MainCir {

  public static Program doCir(Program cprog, String debugdir) {
    RangeReplacer.process(cprog);
    CWriter.print(cprog, debugdir + "norange.rzy", true);

    BlockReduction.process(cprog);

    makeCLibrary(cprog.getLibrary());

    BoolToEnum.process(cprog);
    CArrayCopy.process(cprog);
    VarDeclToTop.process(cprog);

    Renamer.process(cprog);

    toposort(cprog.getType());
    return cprog;
  }

  private static void makeCLibrary(List<CLibrary> list) {
    list.add(makeClibString());
  }

  private static CLibrary makeClibString() {
    Set<FuncAttr> attr = new HashSet<FuncAttr>();
    attr.add(FuncAttr.Public);

    CLibrary ret = new CLibrary("string");

    { // memcpy
      UIntType uint4 = new UIntType(4); // FIXME get singleton

      List<FuncVariable> arg = new ArrayList<FuncVariable>();
      PointerType voidp = new PointerType("voidp_t", new TypeRef(new VoidType())); // FIXME get void singleton
      arg.add(new FuncVariable("dstp", new cir.type.TypeRef(voidp)));
      arg.add(new FuncVariable("srcp", new cir.type.TypeRef(voidp)));
      arg.add(new FuncVariable("size", new cir.type.TypeRef(uint4)));
      LibFunction memcpy = new LibFunction("memcpy", new cir.type.TypeRef(voidp), arg, new HashSet<FuncAttr>(attr));
      ret.getFunction().add(memcpy);
    }

    return ret;
  }

  private static void toposort(List<cir.type.Type> list) {
    SimpleGraph<cir.type.Type> g = new SimpleGraph<cir.type.Type>();
    for (cir.type.Type u : list) {
      g.addVertex(u);
      Set<cir.type.Type> vs = getDirectUsedTypes(u);
      for (Type v : vs) {
        g.addVertex(v);
        g.addEdge(u, v);
      }
    }

    ArrayList<cir.type.Type> old = new ArrayList<cir.type.Type>(list);
    int size = list.size();
    list.clear();
    LinkedList<cir.type.Type> nlist = new LinkedList<cir.type.Type>();
    TopologicalOrderIterator<cir.type.Type, Pair<cir.type.Type, cir.type.Type>> itr = new TopologicalOrderIterator<cir.type.Type, Pair<cir.type.Type, cir.type.Type>>(g);
    while (itr.hasNext()) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<cir.type.Type> diff = new ArrayList<cir.type.Type>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert (size == list.size());
  }

  private static Set<cir.type.Type> getDirectUsedTypes(cir.type.Type u) {
    cir.DefTraverser<Void, Set<cir.type.Type>> getter = new cir.DefTraverser<Void, Set<cir.type.Type>>() {
      @Override
      protected Void visitTypeRef(cir.type.TypeRef obj, Set<cir.type.Type> param) {
        param.add(obj.getRef());
        return null;
      }
    };
    Set<cir.type.Type> vs = new HashSet<cir.type.Type>();
    getter.traverse(u, vs);
    return vs;
  }

  private static void printFpcHeader(String outdir, String name, cir.other.Program cprog) {
    String cfilename = outdir + name + ".pas";
    FpcHeaderWriter cwriter = new FpcHeaderWriter();
    try {
      cwriter.traverse(cprog, new StreamWriter(new PrintStream(cfilename)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

}
