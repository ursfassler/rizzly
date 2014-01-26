package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import util.Pair;
import util.SimpleGraph;
import cir.other.Program;
import cir.traverser.BlockReduction;
import cir.traverser.CWriter;
import cir.traverser.RangeReplacer;
import cir.traverser.Renamer;
import cir.traverser.VarDeclToTop;
import cir.type.Type;

public class MainCir {

  public static Program doCir(Program cprog, String debugdir) {
    RangeReplacer.process(cprog);
    CWriter.print(cprog, debugdir + "norange.rzy", true);

    BlockReduction.process(cprog);

    VarDeclToTop.process(cprog);

    Renamer.process(cprog);

    toposort(cprog.getType());
    return cprog;
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

}
