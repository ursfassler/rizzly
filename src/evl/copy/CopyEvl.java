package evl.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.Expression;
import evl.expression.reference.RefItem;
import evl.function.FunctionBase;
import evl.hfsm.StateItem;
import evl.other.CompUse;
import evl.other.ImplElementary;
import evl.other.Named;
import evl.other.NamedList;
import evl.other.RizzlyProgram;
import evl.statement.CaseOpt;
import evl.statement.CaseOptEntry;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.IfOption;
import evl.statement.Statement;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.EnumDefRef;
import evl.type.base.EnumElement;
import evl.type.composed.NamedElement;
import evl.variable.Variable;

class CopyEvl extends NullTraverser<Evl, Void> {
  // / keeps the old -> new Named objects in order to relink references
  private Map<Named, Named> copied = new HashMap<Named, Named>();
  private CopyFunction func = new CopyFunction(this);
  private CopyVariable var = new CopyVariable(this);
  private CopyExpression expr = new CopyExpression(this);
  private CopyType type = new CopyType(this);
  private CopyStatement stmt = new CopyStatement(this);
  private CopyRef ref = new CopyRef(this);
  private CopyStateItem cosi = new CopyStateItem(this);

  public Map<Named, Named> getCopied() {
    return copied;
  }

  @SuppressWarnings("unchecked")
  public <T extends Evl> T copy(T obj) {
    return (T) visit(obj, null);
  }

  public <T extends Evl> Collection<T> copy(Collection<T> obj) {
    ArrayList<T> ret = new ArrayList<T>();
    for (T itr : obj) {
      ret.add(copy(itr));
    }
    return ret;
  }

  @Override
  protected Evl visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Evl visit(Evl obj, Void param) {
    if (obj instanceof Named) {
      if (copied.containsKey(obj)) {
        Named ret = copied.get(obj);
        assert (ret != null);
        return ret;
      } else {
        Evl nobj = super.visit(obj, param);
        assert (nobj instanceof Named);
        copied.put((Named) obj, (Named) nobj);
        return nobj;
      }
    }
    return super.visit(obj, param);
  }

  @Override
  protected Evl visitRizzlyProgram(RizzlyProgram obj, Void param) {
    RizzlyProgram ret = new RizzlyProgram(obj.getRootdir(), obj.getName());
    ret.getConstant().addAll(copy(obj.getConstant().getList()));
    ret.getFunction().addAll(copy(obj.getFunction().getList()));
    ret.getType().addAll(copy(obj.getType().getList()));
    ret.getVariable().addAll(copy(obj.getVariable().getList()));
    return ret;
  }

  @Override
  protected Evl visitNamedList(NamedList<Named> obj, Void param) {
    NamedList<Named> ret = new NamedList<Named>(obj.getInfo(), obj.getName());
    ret.addAll(copy(obj.getList()));
    return ret;
  }

  @Override
  protected Evl visitFunctionBase(FunctionBase obj, Void param) {
    return func.traverse(obj, param);
  }

  @Override
  protected Evl visitVariable(Variable obj, Void param) {
    return var.traverse(obj, param);
  }

  @Override
  protected Evl visitExpression(Expression obj, Void param) {
    return expr.traverse(obj, param);
  }

  @Override
  protected Evl visitType(Type obj, Void param) {
    return type.traverse(obj, param);
  }

  @Override
  protected Evl visitStatement(Statement obj, Void param) {
    return stmt.traverse(obj, param);
  }

  @Override
  protected Evl visitRefItem(RefItem obj, Void param) {
    return ref.traverse(obj, param);
  }

  @Override
  protected Evl visitStateItem(StateItem obj, Void param) {
    return cosi.traverse(obj, param);
  }

  @Override
  protected Evl visitCompUse(CompUse obj, Void param) {
    return new CompUse(obj.getInfo(), obj.getName(), obj.getLink()); // we keep link to old Component
  }

  @Override
  protected Evl visitImplElementary(ImplElementary obj, Void param) {
    ImplElementary ret = new ImplElementary(obj.getInfo(), obj.getName());

    ret.getInput().addAll(copy(obj.getInput().getList()));
    ret.getOutput().addAll(copy(obj.getOutput().getList()));
    ret.getVariable().addAll(copy(obj.getVariable().getList()));
    ret.getConstant().addAll(copy(obj.getConstant().getList()));
    ret.getComponent().addAll(copy(obj.getComponent().getList()));
    ret.getInternalFunction().addAll(copy(obj.getInternalFunction().getList()));
    ret.getSubComCallback().addAll(copy(obj.getSubComCallback().getList()));
    ret.setEntryFunc(copy(obj.getEntryFunc()));
    ret.setExitFunc(copy(obj.getExitFunc()));

    return ret;
  }

  @Override
  protected Evl visitTypeRef(TypeRef obj, Void param) {
    return new TypeRef(obj.getInfo(), obj.getRef()); // we keep link to old type
  }

  @Override
  protected Evl visitNamedElement(NamedElement obj, Void param) {
    return new NamedElement(obj.getInfo(), obj.getName(), copy(obj.getType()));
  }

  @Override
  protected Evl visitEnumDefRef(EnumDefRef obj, Void param) {
    EnumElement elem = copy(obj.getElem());
    if (copied.containsKey(obj)) {
      return copied.get(obj);
    } else {
      return new EnumDefRef(obj.getInfo(), elem);
    }
  }

  @Override
  protected Evl visitIfOption(IfOption obj, Void param) {
    return new IfOption(obj.getInfo(), copy(obj.getCondition()), copy(obj.getCode()));
  }

  @Override
  protected Evl visitCaseOpt(CaseOpt obj, Void param) {
    List<CaseOptEntry> entries = new ArrayList<CaseOptEntry>();
    for (CaseOptEntry itr : obj.getValue()) {
      entries.add(copy(itr));
    }
    return new CaseOpt(obj.getInfo(), entries, copy(obj.getCode()));
  }

  @Override
  protected Evl visitCaseOptValue(CaseOptValue obj, Void param) {
    return new CaseOptValue(obj.getInfo(), copy(obj.getValue()));
  }

  @Override
  protected Evl visitCaseOptRange(CaseOptRange obj, Void param) {
    return new CaseOptRange(obj.getInfo(), copy(obj.getStart()), copy(obj.getEnd()));
  }

}
