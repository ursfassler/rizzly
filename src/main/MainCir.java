package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import util.Pair;
import util.SimpleGraph;
import cir.expression.reference.Reference;
import cir.other.Program;
import cir.traverser.BlockReduction;
import cir.traverser.CWriter;
import cir.traverser.RangeReplacer;
import cir.traverser.Renamer;
import cir.traverser.VarDeclToTop;
import cir.type.Type;
import cir.type.TypeRef;
import cir.variable.Variable;

public class MainCir {

  public static Program doCir(Program cprog, String debugdir) {
    CWriter.print(cprog, debugdir + "firstCir.rzy", true);
    RangeReplacer.process(cprog);
    CWriter.print(cprog, debugdir + "norange.rzy", true);

    BlockReduction.process(cprog);

    VarDeclToTop.process(cprog);

    Renamer.process(cprog);

    toposort(cprog.getType());
    toposortVar(cprog.getVariable());
    return cprog;
  }

  private static void toposortVar(List<Variable> list) {
    SimpleGraph<Variable> g = new SimpleGraph<Variable>();
    for (Variable u : list) {
      g.addVertex(u);
      Set<Variable> vs = getDirectUsedVariables(u);
      for (Variable v : vs) {
        g.addVertex(v);
        g.addEdge(u, v);
      }
    }

    ArrayList<Variable> old = new ArrayList<Variable>(list);
    int size = list.size();
    list.clear();
    LinkedList<Variable> nlist = new LinkedList<Variable>();
    TopologicalOrderIterator<Variable, Pair<Variable, Variable>> itr = new TopologicalOrderIterator<Variable, Pair<Variable, Variable>>(g);
    while (itr.hasNext()) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<Variable> diff = new ArrayList<Variable>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert (size == list.size());
  }

  private static Set<Variable> getDirectUsedVariables(Variable u) {
    cir.DefTraverser<Void, Set<Variable>> getter = new cir.DefTraverser<Void, Set<Variable>>() {

      @Override
      protected Void visitReference(Reference obj, Set<Variable> param) {
        if (obj.getRef() instanceof Variable) {
          param.add((Variable) obj.getRef());
        }
        return super.visitReference(obj, param);
      }
    };
    Set<Variable> vs = new HashSet<Variable>();
    getter.traverse(u, vs);
    return vs;
  }

  private static void toposort(List<Type> list) {
    SimpleGraph<Type> g = new SimpleGraph<Type>();
    for (Type u : list) {
      g.addVertex(u);
      Set<Type> vs = getDirectUsedTypes(u);
      for (Type v : vs) {
        g.addVertex(v);
        g.addEdge(u, v);
      }
    }

    ArrayList<Type> old = new ArrayList<Type>(list);
    int size = list.size();
    list.clear();
    LinkedList<Type> nlist = new LinkedList<Type>();
    TopologicalOrderIterator<Type, Pair<Type, Type>> itr = new TopologicalOrderIterator<Type, Pair<Type, Type>>(g);
    while (itr.hasNext()) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<Type> diff = new ArrayList<Type>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert (size == list.size());
  }

  private static Set<Type> getDirectUsedTypes(Type u) {
    cir.DefTraverser<Void, Set<Type>> getter = new cir.DefTraverser<Void, Set<Type>>() {
      @Override
      protected Void visitTypeRef(TypeRef obj, Set<Type> param) {
        param.add(obj.getRef());
        return null;
      }
    };
    Set<Type> vs = new HashSet<Type>();
    getter.traverse(u, vs);
    return vs;
  }

}
