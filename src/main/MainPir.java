package main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import joGraph.HtmlGraphWriter;
import joGraph.Writer;

import org.jgrapht.Graph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import pir.PirObject;
import pir.statement.bbend.CaseGoto;
import pir.statement.bbend.Goto;
import pir.statement.bbend.IfGoto;
import pir.statement.bbend.ReturnExpr;
import pir.statement.bbend.ReturnVoid;
import pir.know.KnowledgeBase;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.passes.CaseReduction;
import pir.passes.ConstPropagator;
import pir.passes.LlvmIntTypeReplacer;
import pir.passes.RangeConverter;
import pir.passes.RangeReplacer;
import pir.passes.StmtSignSetter;
import pir.passes.TypecastReplacer;
import pir.passes.VarPropagator;
import pir.statement.normal.CallAssignment;
import pir.statement.normal.CallStmt;
import pir.statement.Statement;
import pir.statement.normal.StoreStmt;
import pir.traverser.DependencyGraphMaker;
import pir.traverser.LlvmWriter;
import pir.traverser.OwnerMap;
import pir.traverser.Renamer;
import pir.traverser.StmtRemover;
import pir.traverser.TyperefCounter;
import pir.type.Type;
import util.GraphHelper;
import util.Pair;
import util.SimpleGraph;

/**
 *
 * @author urs
 */
public class MainPir {

  public static void doPir(Program prog, String debugdir) {
    LlvmWriter.print(prog, debugdir + "afterEvl.ll", true);


    KnowledgeBase kb = new KnowledgeBase(prog, debugdir);
    { // reducing Range and boolean to nosign type
      RangeConverter.process(prog, kb);
      RangeReplacer.process(prog);
      TypecastReplacer.process(prog);
      StmtSignSetter.process(prog);
      LlvmIntTypeReplacer.process(prog, kb);
    }

    LlvmWriter.print(prog, debugdir + "typeext.ll", true);

    CaseReduction.process(prog);

    VarPropagator.process(prog);
    ConstPropagator.process(prog);

    { // remove unused statements
      HashMap<SsaVariable, Statement> owner = OwnerMap.make(prog);
      SimpleGraph<PirObject> g = DependencyGraphMaker.make(prog, owner);

      PirObject rootDummy = new PirObject() {
      };

      Set<Class<? extends Statement>> keep = new HashSet<Class<? extends Statement>>();
      keep.add(CallAssignment.class);
      keep.add(CallStmt.class);
      keep.add(StoreStmt.class);

      keep.add(CaseGoto.class);
      keep.add(Goto.class);
      keep.add(IfGoto.class);
      keep.add(ReturnExpr.class);
      keep.add(ReturnVoid.class);

      Set<Statement> removable = new HashSet<Statement>();

      g.addVertex(rootDummy);
      for( PirObject obj : g.vertexSet() ) {
        if( keep.contains(obj.getClass()) ) {
          g.addEdge(rootDummy, obj);
        }
        if( obj instanceof Statement ) {
          removable.add((Statement) obj);
        }
      }

      GraphHelper.doTransitiveClosure(g);
      printGraph(g, debugdir + "pirdepstmt.gv");

      removable.removeAll(g.getOutVertices(rootDummy));

      StmtRemover.process(prog, removable);
    }

    { // remove unused types
      // FIXME use dependency graph and transitive closure to remove all unused
      Map<Type, Integer> count = TyperefCounter.process(prog);
      Set<Type> removable = new HashSet<Type>();
      for( Type type : count.keySet() ) {
        if( count.get(type) == 0 ) {
          removable.add(type);
        }
      }
      prog.getType().removeAll(removable);
    }

    Renamer.process(prog);

    toposortPir(prog.getType());
  }

  //TODO make sorting independent of type, i.e. that it can be used for everything
  private static void toposortPir(List<Type> list) {
    SimpleGraph<Type> g = new SimpleGraph<Type>(list);
    for( Type u : list ) {
      Set<Type> vs = getDirectUsedTypesPir(u);
      for( Type v : vs ) {
        g.addEdge(u, v);
      }
    }

    ArrayList<Type> old = new ArrayList<Type>(list);
    int size = list.size();
    list.clear();
    LinkedList<Type> nlist = new LinkedList<Type>();
    TopologicalOrderIterator<Type, Pair<Type, Type>> itr = new TopologicalOrderIterator<Type, Pair<Type, Type>>(g);
    while( itr.hasNext() ) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<Type> diff = new ArrayList<Type>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert ( size == list.size() );
  }

  private static Set<Type> getDirectUsedTypesPir(Type u) {
    pir.DefTraverser<Void, Set<Type>> getter = new pir.DefTraverser<Void, Set<Type>>() {

      @Override
      protected Void visitTypeRef(pir.type.TypeRef obj, Set<Type> param) {
        param.add(obj.getRef());
        return null;
      }
    };
    Set<Type> vs = new HashSet<Type>();
    getter.traverse(u, vs);
    return vs;
  }

  private static void printGraph(Graph<PirObject, Pair<PirObject, PirObject>> g, String filename) {
    HtmlGraphWriter<PirObject, Pair<PirObject, PirObject>> hgw;
    try {
      hgw = new HtmlGraphWriter<PirObject, Pair<PirObject, PirObject>>(new Writer(new PrintStream(filename))) {

        @Override
        protected void wrVertex(PirObject v) {
          wrVertexStart(v);
          wrRow(v.toString());
          wrVertexEnd();
        }
      };
      hgw.print(g);
    } catch( FileNotFoundException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
