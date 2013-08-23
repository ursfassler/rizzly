package pir.passes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pir.expression.Number;
import pir.expression.reference.VarRefSimple;
import pir.other.PirValue;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.statement.Assignment;
import pir.statement.Statement;
import pir.traverser.PirValueReplacer;
import pir.traverser.StatementReplacer;

/**
 * Propagate (simple) constants (over assignments, make them obsolete and removes them)
 * 
 * @author urs
 * 
 */
public class ConstPropagator extends StatementReplacer<Map<SsaVariable, BigInteger>> {

  public static void process(Program obj) {
    ConstPropagator changer = new ConstPropagator();
    Map<SsaVariable, BigInteger> map = new HashMap<SsaVariable, BigInteger>();
    changer.traverse(obj, map);
    PvRelinker relinker = new PvRelinker();
    relinker.traverse(obj, map);
  }

  @Override
  protected List<Statement> visitAssignment(Assignment obj, Map<SsaVariable, BigInteger> param) {
    if (obj.getSrc() instanceof Number) {
      param.put(obj.getVariable(), ((Number) obj.getSrc()).getValue());
      return new ArrayList<Statement>();
    } else {
      return null; // in VarPropagator
    }
  }

}

class PvRelinker extends PirValueReplacer<Void, Map<SsaVariable, BigInteger>> {

  @Override
  protected PirValue replace(PirValue val, Map<SsaVariable, BigInteger> param) {
    if (val instanceof VarRefSimple) {
      SsaVariable target = ((VarRefSimple) val).getRef();
      if (param.containsKey(target)) {
        return new Number(param.get(target));
      }
    }
    return val;
  }

}

