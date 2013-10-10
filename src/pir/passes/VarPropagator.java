package pir.passes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import pir.cfg.BasicBlock;
import pir.expression.reference.VarRefSimple;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.statement.normal.Assignment;
import pir.statement.normal.NormalStmt;
import pir.statement.phi.PhiStmt;
import pir.traverser.Relinker;
import pir.traverser.StatementReplacer;

/**
 * Propagate SsaVariables (over assignments, make them obsolete and removes them)
 * 
 * @author urs
 * 
 */
public class VarPropagator extends StatementReplacer<Map<SsaVariable, SsaVariable>> {

  public static void process(Program obj) {
    VarPropagator changer = new VarPropagator();
    Map<SsaVariable, SsaVariable> map = new HashMap<SsaVariable, SsaVariable>();
    changer.traverse(obj, map);
    closure(map);
    Relinker.process(obj, map);
  }

  private static void closure(Map<SsaVariable, SsaVariable> map) {
    for( SsaVariable src : new HashSet<SsaVariable>(map.keySet()) ) {
      SsaVariable last = follow(src, map);
      map.put(src, last);
    }
  }

  private static SsaVariable follow(SsaVariable src, Map<SsaVariable, SsaVariable> map) {
    while( map.containsKey(src) ) {
      src = map.get(src);
    }
    return src;
  }

  @Override
  protected List<NormalStmt> visitAssignment(Assignment obj, Map<SsaVariable, SsaVariable> param) {
    if( obj.getSrc() instanceof VarRefSimple ) {
      param.put(obj.getVariable(), ( (VarRefSimple) obj.getSrc() ).getRef());
      return new ArrayList<NormalStmt>();
    } else {
      return null; // in ConstPropagator
    }
  }
}
