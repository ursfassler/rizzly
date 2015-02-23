/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package evl.traverser;

import java.util.ArrayList;
import java.util.List;

import pass.EvlPass;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.TupleValue;
import evl.expression.TypeCast;
import evl.expression.binop.BinaryExp;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowSimpleExpr;
import evl.knowledge.KnowType;
import evl.knowledge.KnowUniqueName;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.other.SubCallbacks;
import evl.statement.AssignmentMulti;
import evl.statement.AssignmentSingle;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;
import evl.statement.CaseStmt;
import evl.statement.IfOption;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;
import evl.type.Type;
import evl.variable.FuncVariable;

//TODO also split relation operands (only ref to var or constant)
//TODO check if everything is still ok
/**
 * Splits expression down to two operands per expression and maximal one function call.
 *
 * @author urs
 *
 */
public class ExprCutter extends EvlPass {
  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ExprCutterWorker cutter = new ExprCutterWorker(kb);
    cutter.traverse(evl, null);
  }
}

class ExprCutterWorker extends NullTraverser<Void, Void> {

  private StmtTraverser st;

  public ExprCutterWorker(KnowledgeBase kb) {
    super();
    this.st = new StmtTraverser(kb);
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    // throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    return null;
  }

  @Override
  protected Void visitList(EvlList<? extends Evl> list, Void param) {
    // against comodification error
    EvlList<Evl> old = new EvlList<Evl>();
    for (Evl itr : list) {
      old.add(itr);
    }
    return super.visitList(old, param);
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.getChildren(), param);
    return null;
  }

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, Void param) {
    visitList(obj.getFunc(), param);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Void param) {
    visit(obj.getBody(), null);
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, Void param) {
    st.traverse(obj, null);
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    visitList(obj.getFunction(), param);
    visitList(obj.getSubCallback(), param);
    visitList(obj.getIface(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    return null;
  }
}

class StmtTraverser extends NullTraverser<Void, List<Statement>> {

  private KnowledgeBase kb;
  private Cutter cutter;
  private CallDetector cd = new CallDetector();

  public StmtTraverser(KnowledgeBase kb) {
    super();
    this.kb = kb;
    cutter = new Cutter(kb);
  }

  @Override
  protected Void visitDefault(Evl obj, List<Statement> param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitBlock(Block obj, List<Statement> param) {
    List<Statement> list = new ArrayList<Statement>();
    for (Statement stmt : obj.getStatements()) {
      visit(stmt, list);
      assert (!list.contains(stmt));
      list.add(stmt);
    }
    obj.getStatements().clear();
    obj.getStatements().addAll(list);
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, List<Statement> param) {
    obj.setCall((Reference) cutter.traverse(obj.getCall(), param));
    return null;
  }

  @Override
  protected Void visitAssignmentSingle(AssignmentSingle obj, List<Statement> param) {
    obj.setRight(cutter.traverse(obj.getRight(), param));

    boolean rs = KnowSimpleExpr.isSimple(obj.getRight());
    boolean ls = KnowSimpleExpr.isSimple(obj.getLeft());

    if (!rs && !ls) {
      FuncVariable var = cutter.extract(obj.getRight(), param);
      obj.setRight(new Reference(ElementInfo.NO, var));
    }

    return null;
  }

  @Override
  protected Void visitAssignmentMulti(AssignmentMulti obj, List<Statement> param) {
    obj.setRight(cutter.traverse(obj.getRight(), param));
    visitList(obj.getLeft(), param);

    boolean rs = KnowSimpleExpr.isSimple(obj.getRight());
    boolean ls;

    RError.ass(!obj.getLeft().isEmpty(), obj.getInfo(), "expected at least one item on the left");
    if (obj.getLeft().size() == 1) {
      ls = KnowSimpleExpr.isSimple(obj.getLeft().get(0));
    } else {
      throw new UnsupportedOperationException("Not supported yet");
    }

    if (!rs && !ls) {
      FuncVariable var = cutter.extract(obj.getRight(), param);
      obj.setRight(new Reference(ElementInfo.NO, var));
    }

    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, List<Statement> param) {
    if (!KnowSimpleExpr.isSimple(obj.getValue())) {
      FuncVariable var = cutter.extract(obj.getValue(), param);
      obj.setValue(new Reference(obj.getInfo(), var));
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, List<Statement> param) {
    visitList(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, List<Statement> param) {
    if (!KnowSimpleExpr.isSimple(obj.getIndex())) {
      FuncVariable var = cutter.extract(obj.getIndex(), param);
      obj.setIndex(new Reference(obj.getInfo(), var));
    }
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, List<Statement> param) {
    super.visitRefCall(obj, param);
    EvlList<Expression> value = obj.getActualParameter().getValue();
    for (int i = 0; i < value.size(); i++) {
      Expression expr = value.get(i);
      if (cd.traverse(expr, null)) {
        FuncVariable var = cutter.extract(expr, param);
        value.set(i, new Reference(obj.getInfo(), var));
      }
    }
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, List<Statement> param) {
    return null;
  }

  @Override
  protected Void visitWhileStmt(WhileStmt obj, List<Statement> param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "can not yet correctly split while statement");
    // FIXME the following code is wrong. If we extract code from the condition, it has to go into all incoming edges of
    // the condition
    // => also in the body of the loop
    if (!KnowSimpleExpr.isSimple(obj.getCondition())) {
      FuncVariable var = cutter.extract(obj.getCondition(), param);
      obj.setCondition(new Reference(obj.getInfo(), var));
    }
    visit(obj.getBody(), null);
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, List<Statement> param) {
    for (IfOption opt : obj.getOption()) {
      if (!KnowSimpleExpr.isSimple(opt.getCondition())) {
        FuncVariable var = cutter.extract(opt.getCondition(), param);
        opt.setCondition(new Reference(obj.getInfo(), var));
      }
    }
    for (IfOption opt : obj.getOption()) {
      visit(opt.getCode(), null);
    }
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, List<Statement> param) {
    if (!KnowSimpleExpr.isSimple(obj.getCondition())) {
      FuncVariable var = cutter.extract(obj.getCondition(), param);
      obj.setCondition(new Reference(obj.getInfo(), var));
    }
    for (CaseOpt opt : obj.getOption()) {
      visitList(opt.getValue(), param);
      visit(opt.getCode(), null);
    }
    visit(obj.getOtherwise(), null);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, List<Statement> param) {
    assert (KnowSimpleExpr.isSimple(obj.getValue()));
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, List<Statement> param) {
    assert (KnowSimpleExpr.isSimple(obj.getStart()));
    assert (KnowSimpleExpr.isSimple(obj.getEnd()));
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, List<Statement> param) {
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, List<Statement> param) {
    obj.setExpr(cutter.traverse(obj.getExpr(), param));
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, List<Statement> param) {
    return null;
  }

}

class Cutter extends ExprReplacer<List<Statement>> {
  final private KnowBaseItem kbi;
  final private KnowType kt;
  final private KnowUniqueName kun;
  final private CallDetector cd = new CallDetector();

  public Cutter(KnowledgeBase kb) {
    super();
    kt = kb.getEntry(KnowType.class);
    kbi = kb.getEntry(KnowBaseItem.class);
    kun = kb.getEntry(KnowUniqueName.class);
  }

  private Type getType(Expression obj) {
    Type type = kt.get(obj);
    type = kbi.getType(type);
    return type;
  }

  FuncVariable extract(Expression obj, List<Statement> param) {
    ElementInfo info = obj.getInfo();
    Type type = getType(obj);
    FuncVariable var = new FuncVariable(info, kun.get("locvar"), new SimpleRef<Type>(obj.getInfo(), type));
    param.add(new VarDefStmt(info, var));
    param.add(new AssignmentSingle(info, new Reference(info, var), obj));
    return var;
  }

  @Override
  protected Expression visitFunction(Function obj, List<Statement> param) {
    return super.visitFunction(obj, param);
  }

  @Override
  protected Expression visitReference(Reference obj, List<Statement> param) {
    List<RefItem> olist = new ArrayList<RefItem>(obj.getOffset());
    obj.getOffset().clear();
    Reference ret = obj;
    for (int i = 0; i < olist.size(); i++) {
      RefItem itr = olist.get(i);
      visit(itr, param);
      ret.getOffset().add(itr);
      if ((i < olist.size() - 1) && (itr instanceof RefCall)) {
        FuncVariable var = extract(ret, param);
        ret = new Reference(itr.getInfo(), var);
      }
    }
    return ret;
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, List<Statement> param) {
    super.visitRefIndex(obj, param);
    if (cd.traverse(obj.getIndex(), null)) {
      FuncVariable var = extract(obj.getIndex(), param);
      obj.setIndex(new Reference(obj.getInfo(), var));
    }
    return null;
  }

  @Override
  protected Expression visitTupleValue(TupleValue obj, List<Statement> param) {
    super.visitTupleValue(obj, param);
    for (int i = 0; i < obj.getValue().size(); i++) {
      Expression expr = obj.getValue().get(i);
      if (cd.traverse(expr, null)) {
        FuncVariable var = extract(expr, param);
        obj.getValue().set(i, new Reference(obj.getInfo(), var));
      }
    }
    return obj;
  }

  @Override
  protected Expression visitRefName(RefName obj, List<Statement> param) {
    return super.visitRefName(obj, param);
  }

  @Override
  protected Expression visitBinaryExp(BinaryExp obj, List<Statement> param) {
    obj.setLeft(visit(obj.getLeft(), param));
    obj.setRight(visit(obj.getRight(), param));

    FuncVariable var = extract(obj, param);// TODO needed?

    return new Reference(obj.getInfo(), var);
  }

}

class CallDetector extends NullTraverser<Boolean, Void> {

  @Override
  protected Boolean visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitReference(Reference obj, Void param) {
    for (RefItem itr : obj.getOffset()) {
      if (visit(itr, null)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected Boolean visitRefCall(RefCall obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitRefName(RefName obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitRefIndex(RefIndex obj, Void param) {
    return visit(obj.getIndex(), null);
  }

  @Override
  protected Boolean visitNumber(Number obj, Void param) {
    return false;
  }

  @Override
  protected Boolean visitBoolValue(BoolValue obj, Void param) {
    return false;
  }
}
