package evl.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import evl.DefTraverser;
import evl.cfg.BasicBlockList;
import evl.expression.reference.Reference;
import evl.statement.Statement;
import evl.statement.normal.Assignment;
import evl.variable.StateVariable;

public class KnowStateVariableReadWrite extends KnowledgeEntry {

  final private Map<BasicBlockList, Set<StateVariable>> reads = new HashMap<BasicBlockList, Set<StateVariable>>();
  final private Map<BasicBlockList, Set<StateVariable>> writes = new HashMap<BasicBlockList, Set<StateVariable>>();

  @Override
  public void init(KnowledgeBase base) {
  }

  public Set<StateVariable> getReads(BasicBlockList func) {
    Set<StateVariable> ret = reads.get(func);
    if( ret == null ) {
      update(func);
      ret = reads.get(func);
    }
    assert ( ret != null );
    return ret;
  }

  public Set<StateVariable> getWrites(BasicBlockList func) {
    Set<StateVariable> ret = writes.get(func);
    if( ret == null ) {
      update(func);
      ret = writes.get(func);
    }
    assert ( ret != null );
    return ret;
  }

  private void update(BasicBlockList func) {
    StateVariableTraverser traverser = new StateVariableTraverser();
    traverser.traverse(func, null);
    reads.put(func, traverser.getReads());
    writes.put(func, traverser.getWrites());
  }
}

// param says if we are writing to the reference
class StateVariableTraverser extends DefTraverser<Void, Boolean> {

  final private Set<StateVariable> reads = new HashSet<StateVariable>();
  final private Set<StateVariable> writes = new HashSet<StateVariable>();

  public Set<StateVariable> getReads() {
    return reads;
  }

  public Set<StateVariable> getWrites() {
    return writes;
  }

  @Override
  protected Void visitStatement(Statement obj, Boolean param) {
    assert ( param == null );
    super.visitStatement(obj, false);
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Boolean param) {
    visit(obj.getRight(), false);
    visit(obj.getLeft(), true);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Boolean param) {
    if( obj.getLink() instanceof StateVariable ) {
      StateVariable var = (StateVariable) obj.getLink();
      if( param ) {
        writes.add(var);
      } else {
        reads.add(var);
      }
    }
    visitItr(obj.getOffset(), false);
    return null;
  }
}
