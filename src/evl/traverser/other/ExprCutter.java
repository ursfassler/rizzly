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

package evl.traverser.other;

import java.util.ArrayList;
import java.util.List;

import pass.EvlPass;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.composition.ImplComposition;
import evl.data.component.composition.SubCallbacks;
import evl.data.component.elementary.ImplElementary;
import evl.data.expression.BoolValue;
import evl.data.expression.Expression;
import evl.data.expression.Number;
import evl.data.expression.TupleValue;
import evl.data.expression.TypeCast;
import evl.data.expression.binop.BinaryExp;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefItem;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Block;
import evl.data.statement.CallStmt;
import evl.data.statement.CaseOpt;
import evl.data.statement.CaseOptRange;
import evl.data.statement.CaseOptValue;
import evl.data.statement.CaseStmt;
import evl.data.statement.IfOption;
import evl.data.statement.IfStmt;
import evl.data.statement.ReturnExpr;
import evl.data.statement.ReturnVoid;
import evl.data.statement.Statement;
import evl.data.statement.VarDefStmt;
import evl.data.statement.WhileStmt;
import evl.data.type.Type;
import evl.data.variable.FuncVariable;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowSimpleExpr;
import evl.knowledge.KnowType;
import evl.knowledge.KnowUniqueName;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;

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
    visitList(obj.func, param);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Void param) {
    visit(obj.body, null);
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, Void param) {
    st.traverse(obj, null);
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    visitList(obj.function, param);
    visitList(obj.subCallback, param);
    visitList(obj.iface, param);
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
    for (Statement stmt : obj.statements) {
      visit(stmt, list);
      assert (!list.contains(stmt));
      list.add(stmt);
    }
    obj.statements.clear();
    obj.statements.addAll(list);
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, List<Statement> param) {
    obj.call = (Reference) cutter.traverse(obj.call, param);
    return null;
  }

  @Override
  protected Void visitAssignmentSingle(AssignmentSingle obj, List<Statement> param) {
    obj.right = cutter.traverse(obj.right, param);

    boolean rs = KnowSimpleExpr.isSimple(obj.right);
    boolean ls = KnowSimpleExpr.isSimple(obj.left);

    if (!rs && !ls) {
      FuncVariable var = cutter.extract(obj.right, param);
      obj.right = new Reference(ElementInfo.NO, var);
    }

    return null;
  }

  @Override
  protected Void visitAssignmentMulti(AssignmentMulti obj, List<Statement> param) {
    obj.right = cutter.traverse(obj.right, param);
    visitList(obj.left, param);

    boolean rs = KnowSimpleExpr.isSimple(obj.right);
    boolean ls;

    RError.ass(!obj.left.isEmpty(), obj.getInfo(), "expected at least one item on the left");
    if (obj.left.size() == 1) {
      ls = KnowSimpleExpr.isSimple(obj.left.get(0));
    } else {
      throw new UnsupportedOperationException("Not supported yet");
    }

    if (!rs && !ls) {
      FuncVariable var = cutter.extract(obj.right, param);
      obj.right = new Reference(ElementInfo.NO, var);
    }

    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, List<Statement> param) {
    if (!KnowSimpleExpr.isSimple(obj.value)) {
      FuncVariable var = cutter.extract(obj.value, param);
      obj.value = new Reference(obj.getInfo(), var);
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, List<Statement> param) {
    visitList(obj.offset, param);
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, List<Statement> param) {
    if (!KnowSimpleExpr.isSimple(obj.index)) {
      FuncVariable var = cutter.extract(obj.index, param);
      obj.index = new Reference(obj.getInfo(), var);
    }
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, List<Statement> param) {
    super.visitRefCall(obj, param);
    EvlList<Expression> value = obj.actualParameter.value;
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
    if (!KnowSimpleExpr.isSimple(obj.condition)) {
      FuncVariable var = cutter.extract(obj.condition, param);
      obj.condition = new Reference(obj.getInfo(), var);
    }
    visit(obj.body, null);
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, List<Statement> param) {
    for (IfOption opt : obj.option) {
      if (!KnowSimpleExpr.isSimple(opt.condition)) {
        FuncVariable var = cutter.extract(opt.condition, param);
        opt.condition = new Reference(obj.getInfo(), var);
      }
    }
    for (IfOption opt : obj.option) {
      visit(opt.code, null);
    }
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, List<Statement> param) {
    if (!KnowSimpleExpr.isSimple(obj.condition)) {
      FuncVariable var = cutter.extract(obj.condition, param);
      obj.condition = new Reference(obj.getInfo(), var);
    }
    for (CaseOpt opt : obj.option) {
      visitList(opt.value, param);
      visit(opt.code, null);
    }
    visit(obj.otherwise, null);
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, List<Statement> param) {
    assert (KnowSimpleExpr.isSimple(obj.value));
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, List<Statement> param) {
    assert (KnowSimpleExpr.isSimple(obj.start));
    assert (KnowSimpleExpr.isSimple(obj.end));
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, List<Statement> param) {
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, List<Statement> param) {
    obj.expr = cutter.traverse(obj.expr, param);
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
    List<RefItem> olist = new ArrayList<RefItem>(obj.offset);
    obj.offset.clear();
    Reference ret = obj;
    for (int i = 0; i < olist.size(); i++) {
      RefItem itr = olist.get(i);
      visit(itr, param);
      ret.offset.add(itr);
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
    if (cd.traverse(obj.index, null)) {
      FuncVariable var = extract(obj.index, param);
      obj.index = new Reference(obj.getInfo(), var);
    }
    return null;
  }

  @Override
  protected Expression visitTupleValue(TupleValue obj, List<Statement> param) {
    super.visitTupleValue(obj, param);
    for (int i = 0; i < obj.value.size(); i++) {
      Expression expr = obj.value.get(i);
      if (cd.traverse(expr, null)) {
        FuncVariable var = extract(expr, param);
        obj.value.set(i, new Reference(obj.getInfo(), var));
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
    obj.left = visit(obj.left, param);
    obj.right = visit(obj.right, param);

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
    for (RefItem itr : obj.offset) {
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
    return visit(obj.index, null);
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
