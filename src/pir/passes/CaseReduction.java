package pir.passes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pir.DefTraverser;
import pir.NullTraverser;
import pir.PirObject;
import pir.statement.bbend.CaseGoto;
import pir.statement.bbend.CaseGotoOpt;
import pir.statement.bbend.CaseOptEntry;
import pir.statement.bbend.CaseOptRange;
import pir.statement.bbend.CaseOptValue;
import pir.expression.Number;
import pir.other.Program;
import pir.type.Type;
import pir.type.TypeRef;

/**
 * Ensures that CaseGotoOpt.value.size == 1 and the type of it is a single value
 * 
 * @author urs
 * 
 */
public class CaseReduction extends DefTraverser<Void, Void> {
  private CaseValueAdder adder = new CaseValueAdder();

  public static void process(Program obj) {
    CaseReduction changer = new CaseReduction();
    changer.traverse(obj, null);
  }

  @Override
  protected Void visitCaseGoto(CaseGoto obj, Void param) {
    Type condType = obj.getCondition().getType().getRef();
    Map<BigInteger, CaseGotoOpt> newEntries = new HashMap<BigInteger, CaseGotoOpt>();
    for (CaseGotoOpt entry : obj.getOption()) {
      Set<BigInteger> values = getValues(entry);
      for (BigInteger val : values) {
        List<CaseOptEntry> sv = new ArrayList<CaseOptEntry>(1);
        sv.add(new CaseOptValue(new Number(val, new TypeRef(condType))));
        CaseGotoOpt ncgo = new CaseGotoOpt(sv, entry.getDst());
        assert (!newEntries.containsKey(val));
        newEntries.put(val, ncgo);
      }
    }

    ArrayList<BigInteger> values = new ArrayList<BigInteger>(newEntries.keySet());
    Collections.sort(values);

    obj.getOption().clear();
    for (BigInteger value : values) {
      obj.getOption().add(newEntries.get(value));
    }
    return null;
  }

  private Set<BigInteger> getValues(CaseGotoOpt entry) {
    Set<BigInteger> values = new HashSet<BigInteger>();
    adder.traverse(entry, values);
    return values;
  }

}

class CaseValueAdder extends NullTraverser<Void, Set<BigInteger>> {

  @Override
  protected Void doDefault(PirObject obj, Set<BigInteger> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private static void add(Set<BigInteger> param, BigInteger i) {
    assert (!param.contains(i));
    param.add(i);
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Set<BigInteger> param) {
    assert (obj.getStart().compareTo(obj.getEnd()) < 0);
    for (BigInteger i = obj.getStart(); i.compareTo(obj.getEnd()) <= 0; i = i.add(BigInteger.ONE)) {
      add(param, i);
    }
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Set<BigInteger> param) {
    add(param, obj.getValue().getValue());
    return null;
  }

  @Override
  protected Void visitCaseGotoOpt(CaseGotoOpt obj, Set<BigInteger> param) {
    visitList(obj.getValue(), param);
    return null;
  }

}
