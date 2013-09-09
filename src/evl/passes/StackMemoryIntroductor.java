package evl.passes;

import common.Designator;
import common.ElementInfo;
import evl.copy.Relinker;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.statement.StackMemoryAlloc;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.traverser.StatementReplacer;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.special.PointerType;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replaces stack variables with a pointer to it and allocates stack memory for them
 * @author urs
 */
public class StackMemoryIntroductor {

  public static void process(Namespace aclasses, KnowledgeBase kb) {
    StackMemTrav smt = new StackMemTrav(kb);
    smt.traverse(aclasses, null);
    Relinker.relink(aclasses, smt.getMap());
  }
}

class StackMemTrav extends StatementReplacer<Void> {

  final private Map<FuncVariable, SsaVariable> map = new HashMap<FuncVariable, SsaVariable>();
  private KnowBaseItem kbi;

  public StackMemTrav(KnowledgeBase kb) {
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  public Map<FuncVariable, SsaVariable> getMap() {
    return map;
  }

  @Override
  protected List<Statement> visitVarDef(VarDefStmt obj, Void param) {
    Type type = obj.getVariable().getType().getRef();

    PointerType pt = kbi.getPointerType(new TypeRef(new ElementInfo(), type));
    SsaVariable var = new SsaVariable(obj.getVariable().getInfo(), obj.getVariable().getName() + Designator.NAME_SEP + "p", new TypeRef(new ElementInfo(), pt));
    StackMemoryAlloc sma = new StackMemoryAlloc(obj.getInfo(), var);

    map.put(obj.getVariable(), var);

    return ret(sma);
  }
}
