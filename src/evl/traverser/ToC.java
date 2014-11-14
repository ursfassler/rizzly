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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Range;
import cir.CirBase;
import cir.expression.BinaryOp;
import cir.expression.ElementValue;
import cir.expression.NoValue;
import cir.expression.Op;
import cir.expression.reference.RefItem;
import cir.function.FunctionImpl;
import cir.function.FunctionPrivate;
import cir.function.FunctionPrototype;
import cir.function.FunctionPublic;
import cir.type.NamedElement;
import cir.type.TypeRef;
import cir.variable.Variable;

import common.Designator;
import common.Property;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.AnyValue;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.NamedElementValue;
import evl.expression.Number;
import evl.expression.RecordValue;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.UnionValue;
import evl.expression.UnsafeUnionValue;
import evl.expression.binop.BinaryExp;
import evl.expression.binop.BitAnd;
import evl.expression.binop.BitOr;
import evl.expression.binop.Div;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.LogicAnd;
import evl.expression.binop.LogicOr;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.expression.unop.BitNot;
import evl.expression.unop.LogicNot;
import evl.expression.unop.Uminus;
import evl.other.Named;
import evl.other.RizzlyProgram;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseOptEntry;
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
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.type.special.VoidType;
import evl.variable.ConstGlobal;
import evl.variable.ConstPrivate;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;

public class ToC extends NullTraverser<CirBase, String> {
  final private Map<Evl, CirBase> map = new HashMap<Evl, CirBase>();

  public static CirBase process(Evl obj) {
    ToC toC = new ToC();
    return toC.traverse(obj, null);
  }

  private int toInt(BigInteger bigval) {
    if (bigval.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
      assert (false); // TODO better error
    }
    if (bigval.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
      assert (false); // TODO better error
    }
    int intValue = bigval.intValue();
    return intValue;
  }

  private String getName(Named obj, String param) {
    if (obj.getName().length() == 0) {
      obj.setName(Designator.NAME_SEP + Integer.toHexString(obj.hashCode()));
      RError.err(ErrorType.Warning, obj.getInfo(), obj.getClass().getSimpleName() + " with no name found! Calling it " + obj.getName());
    }
    return obj.getName();
  }

  @Override
  protected CirBase visitDefault(Evl obj, String param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected CirBase visit(Evl obj, String param) {
    CirBase cobj = map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected cir.other.Program visitRizzlyProgram(RizzlyProgram obj, String param) {
    cir.other.Program prog = new cir.other.Program(obj.getName());
    for (Type type : obj.getType()) {
      cir.type.Type ct = (cir.type.Type) visit(type, param);
      prog.getType().add(ct);
    }
    for (StateVariable itr : obj.getVariable()) {
      Variable ct = (Variable) visit(itr, param);
      prog.getVariable().add(ct);
    }
    for (Constant itr : obj.getConstant()) {
      cir.variable.Constant ct = (cir.variable.Constant) visit(itr, param);
      prog.getVariable().add(ct);
    }
    for (evl.function.Function itr : obj.getFunction()) {
      cir.function.Function ct = (cir.function.Function) visit(itr, param);
      prog.getFunction().add(ct);
    }
    return prog;
  }

  @Override
  protected CirBase visitFunctionImpl(evl.function.Function obj, String param) {
    List<cir.variable.FuncVariable> arg = new ArrayList<cir.variable.FuncVariable>();
    for (FuncVariable var : obj.getParam()) {
      arg.add((cir.variable.FuncVariable) visit(var, param));
    }

    cir.type.TypeRef rettype = (cir.type.TypeRef) visit(obj.getRet(), null);

    cir.function.Function ret;
    ret = createFunc(obj, getName(obj, param), arg, rettype);

    map.put(obj, ret); // otherwise the compiler follows recursive calls
    cir.statement.Block body = (cir.statement.Block) visit(obj.getBody(), param);
    if (ret instanceof FunctionImpl) {
      ((FunctionImpl) ret).getBody().getStatement().addAll(body.getStatement());
    }

    return ret;
  }

  private cir.function.Function createFunc(evl.function.Function obj, String name, List<cir.variable.FuncVariable> arg, cir.type.TypeRef rettype) {
    if (obj.properties().get(Property.Extern) == Boolean.TRUE) {
      return new FunctionPrototype(name, rettype, arg);
    } else if (obj.properties().get(Property.Public) == Boolean.TRUE) {
      return new FunctionPublic(name, rettype, arg, new cir.statement.Block());
    } else {
      return new FunctionPrivate(name, rettype, arg, new cir.statement.Block());
    }
  }

  @Override
  protected CirBase visitTypeCast(TypeCast obj, String param) {
    return new cir.expression.TypeCast((cir.type.TypeRef) visit(obj.getCast(), null), (cir.expression.Expression) visit(obj.getValue(), null));
  }

  @Override
  protected CirBase visitRefCall(RefCall obj, String param) {
    cir.expression.reference.RefCall call = new cir.expression.reference.RefCall();
    for (Expression expr : obj.getActualParameter()) {
      cir.expression.Expression actarg = (cir.expression.Expression) visit(expr, param);
      call.getParameter().add(actarg);
    }
    return call;
  }

  @Override
  protected CirBase visitRefIndex(RefIndex obj, String param) {
    return new cir.expression.reference.RefIndex((cir.expression.Expression) visit(obj.getIndex(), param));
  }

  @Override
  protected CirBase visitRefName(RefName obj, String param) {
    return new cir.expression.reference.RefName(obj.getName());
  }

  @Override
  protected CirBase visitReturnVoid(ReturnVoid obj, String param) {
    return new cir.statement.ReturnVoid();
  }

  @Override
  protected CirBase visitReturnExpr(ReturnExpr obj, String param) {
    return new cir.statement.ReturnExpr((cir.expression.Expression) visit(obj.getExpr(), param));
  }

  @Override
  protected CirBase visitCaseOpt(CaseOpt obj, String param) {
    cir.statement.CaseEntry ret = new cir.statement.CaseEntry((cir.statement.Block) visit(obj.getCode(), param));
    for (CaseOptEntry entry : obj.getValue()) {
      if (entry instanceof CaseOptRange) {
        cir.expression.Number start = (cir.expression.Number) visit(((CaseOptRange) entry).getStart(), param);
        cir.expression.Number end = (cir.expression.Number) visit(((CaseOptRange) entry).getEnd(), param);
        ret.getValues().add(new Range(start.getValue(), end.getValue()));
      } else if (entry instanceof CaseOptValue) {
        cir.expression.Number num = (cir.expression.Number) visit(((CaseOptValue) entry).getValue(), param);
        ret.getValues().add(new Range(num.getValue(), num.getValue()));
      } else {
        throw new RuntimeException("not yet implemented");
      }
    }
    return ret;
  }

  @Override
  protected CirBase visitConstGlobal(ConstGlobal obj, String param) {
    return new cir.variable.Constant(getName(obj, param), (cir.type.TypeRef) visit(obj.getType(), param), (cir.expression.Expression) visit(obj.getDef(), param));
  }

  @Override
  protected CirBase visitConstPrivate(ConstPrivate obj, String param) {
    return new cir.variable.Constant(getName(obj, param), (cir.type.TypeRef) visit(obj.getType(), param), (cir.expression.Expression) visit(obj.getDef(), param));
  }

  @Override
  protected CirBase visitStateVariable(StateVariable obj, String param) {
    return new cir.variable.StateVariable(getName(obj, param), (cir.type.TypeRef) visit(obj.getType(), param), (cir.expression.Expression) visit(obj.getDef(), param));
  }

  @Override
  protected CirBase visitFuncVariable(FuncVariable obj, String param) {
    cir.variable.FuncVariable var = new cir.variable.FuncVariable(getName(obj, param), (cir.type.TypeRef) visit(obj.getType(), param));
    return var;
  }

  @Override
  protected CirBase visitRangeType(RangeType obj, String param) {
    return new cir.type.RangeType(getName(obj, param), obj.getNumbers().getLow(), obj.getNumbers().getHigh());
  }

  @Override
  protected CirBase visitTypeRef(SimpleRef obj, String param) {
    return new cir.type.TypeRef((cir.type.Type) visit(obj.getLink(), param));
  }

  @Override
  protected CirBase visitVoidType(VoidType obj, String param) {
    return new cir.type.VoidType(getName(obj, param));
  }

  @Override
  protected CirBase visitNumber(Number obj, String param) {
    return new cir.expression.Number(obj.getValue());
  }

  @Override
  protected CirBase visitReference(Reference obj, String param) {
    cir.expression.reference.Reference ref = new cir.expression.reference.Reference((cir.expression.reference.Referencable) visit(obj.getLink(), param));
    for (evl.expression.reference.RefItem itr : obj.getOffset()) {
      ref.getOffset().add((RefItem) visit(itr, null));
    }
    return ref;
  }

  @Override
  protected CirBase visitVarDef(VarDefStmt obj, String param) {
    cir.statement.VarDefStmt stmt = new cir.statement.VarDefStmt((cir.variable.FuncVariable) visit(obj.getVariable(), param));
    return stmt;
  }

  @Override
  protected CirBase visitCallStmt(CallStmt obj, String param) {
    return new cir.statement.CallStmt((cir.expression.reference.Reference) visit(obj.getCall(), param));
  }

  @Override
  protected CirBase visitAssignment(Assignment obj, String param) {
    return new cir.statement.Assignment((cir.expression.reference.Reference) visit(obj.getLeft(), param), (cir.expression.Expression) visit(obj.getRight(), param));
  }

  @Override
  protected CirBase visitCaseStmt(CaseStmt obj, String param) {
    cir.statement.CaseStmt stmt = new cir.statement.CaseStmt((cir.expression.Expression) visit(obj.getCondition(), param), (cir.statement.Block) visit(obj.getOtherwise(), param));
    for (CaseOpt entry : obj.getOption()) {
      cir.statement.CaseEntry centry = (cir.statement.CaseEntry) visit(entry, param);
      stmt.getEntries().add(centry);
    }
    return stmt;
  }

  @Override
  protected CirBase visitWhileStmt(WhileStmt obj, String param) {
    return new cir.statement.WhileStmt((cir.expression.Expression) visit(obj.getCondition(), param), (cir.statement.Block) visit(obj.getBody(), param));
  }

  @Override
  protected CirBase visitIfStmt(IfStmt obj, String param) {
    assert (obj.getOption().size() == 1);
    IfOption opt = obj.getOption().get(0);
    cir.statement.Block elseBlock = (cir.statement.Block) visit(obj.getDefblock(), param);
    cir.statement.Block thenBlock = (cir.statement.Block) visit(opt.getCode(), param);
    cir.expression.Expression cond = (cir.expression.Expression) visit(opt.getCondition(), null);

    return new cir.statement.IfStmt(cond, thenBlock, elseBlock);
  }

  @Override
  protected CirBase visitBlock(Block obj, String param) {
    cir.statement.Block ret = new cir.statement.Block();
    for (Statement stmt : obj.getStatements()) {
      cir.statement.Statement cstmt = (cir.statement.Statement) visit(stmt, param);
      ret.getStatement().add(cstmt);
    }
    return ret;
  }

  @Override
  protected CirBase visitNamedElement(evl.type.composed.NamedElement obj, String param) {
    return new NamedElement(obj.getName(), (TypeRef) visit(obj.getRef(), param));
  }

  @Override
  protected CirBase visitRecordType(RecordType obj, String param) {
    cir.type.StructType type = new cir.type.StructType(getName(obj, param));
    for (evl.type.composed.NamedElement elem : obj.getElement()) {
      cir.type.NamedElement celem = (cir.type.NamedElement) visit(elem, param);
      type.getElements().add(celem);
    }
    return type;
  }

  @Override
  protected CirBase visitUnionType(UnionType obj, String param) {
    cir.type.UnionType type = new cir.type.UnionType(getName(obj, param), (cir.type.NamedElement) visit(obj.getTag(), param));
    for (evl.type.composed.NamedElement elem : obj.getElement()) {
      cir.type.NamedElement celem = (cir.type.NamedElement) visit(elem, param);
      type.getElements().add(celem);
    }
    return type;
  }

  @Override
  protected CirBase visitUnsafeUnionType(UnsafeUnionType obj, String param) {
    cir.type.UnsafeUnionType type = new cir.type.UnsafeUnionType(getName(obj, param));
    for (evl.type.composed.NamedElement elem : obj.getElement()) {
      cir.type.NamedElement celem = (cir.type.NamedElement) visit(elem, param);
      type.getElements().add(celem);
    }
    return type;
  }

  @Override
  protected CirBase visitArrayType(ArrayType obj, String param) {
    return new cir.type.ArrayType(getName(obj, param), (cir.type.TypeRef) visit(obj.getType(), param), toInt(obj.getSize()));
  }

  @Override
  protected CirBase visitStringValue(StringValue obj, String param) {
    return new cir.expression.StringValue(obj.getValue());
  }

  @Override
  protected CirBase visitArrayValue(ArrayValue obj, String param) {
    cir.expression.ArrayValue ret = new cir.expression.ArrayValue();
    for (Expression expr : obj.getValue()) {
      cir.expression.Expression actarg = (cir.expression.Expression) visit(expr, param);
      ret.getValue().add(actarg);
    }
    return ret;
  }

  @Override
  protected CirBase visitRecordValue(RecordValue obj, String param) {
    cir.expression.StructValue ret = new cir.expression.StructValue();
    for (NamedElementValue expr : obj.getValue()) {
      ElementValue actarg = (cir.expression.ElementValue) visit(expr, param);
      ret.getValue().add(actarg);
    }
    return ret;
  }

  @Override
  protected CirBase visitNamedElementValue(NamedElementValue obj, String param) {
    return new cir.expression.ElementValue(obj.getName(), (cir.expression.Expression) visit(obj.getValue(), null));
  }

  @Override
  protected CirBase visitUnsafeUnionValue(UnsafeUnionValue obj, String param) {
    return new cir.expression.UnsafeUnionValue((cir.expression.ElementValue) visit(obj.getContentValue(), param));
  }

  @Override
  protected CirBase visitUnionValue(UnionValue obj, String param) {
    return new cir.expression.UnionValue((cir.expression.ElementValue) visit(obj.getTagValue(), null), (cir.expression.ElementValue) visit(obj.getContentValue(), param));
  }

  @Override
  protected CirBase visitStringType(StringType obj, String param) {
    return new cir.type.StringType();
  }

  @Override
  protected CirBase visitBooleanType(BooleanType obj, String param) {
    return new cir.type.BooleanType(getName(obj, param));
  }

  @Override
  protected CirBase visitBoolValue(BoolValue obj, String param) {
    return new cir.expression.BoolValue(obj.isValue());
  }

  @Override
  protected CirBase visitAnyValue(AnyValue obj, String param) {
    return new NoValue();
  }

  @Override
  protected CirBase visitUminus(Uminus obj, String param) {
    return new cir.expression.UnaryOp(Op.MINUS, (cir.expression.Expression) visit(obj.getExpr(), null));
  }

  @Override
  protected CirBase visitLogicNot(LogicNot obj, String param) {
    return new cir.expression.UnaryOp(Op.LOCNOT, (cir.expression.Expression) visit(obj.getExpr(), null));
  }

  @Override
  protected CirBase visitBitNot(BitNot obj, String param) {
    return new cir.expression.UnaryOp(Op.BITNOT, (cir.expression.Expression) visit(obj.getExpr(), null));
  }

  private CirBase transBinOp(BinaryExp obj, Op op) {
    return new BinaryOp((cir.expression.Expression) visit(obj.getLeft(), null), (cir.expression.Expression) visit(obj.getRight(), null), op);
  }

  @Override
  protected CirBase visitPlus(Plus obj, String param) {
    return transBinOp(obj, Op.PLUS);
  }

  @Override
  protected CirBase visitLogicAnd(LogicAnd obj, String param) {
    return transBinOp(obj, Op.LOCAND);
  }

  @Override
  protected CirBase visitGreaterequal(Greaterequal obj, String param) {
    return transBinOp(obj, Op.GREATER_EQUEAL);
  }

  @Override
  protected CirBase visitNotequal(Notequal obj, String param) {
    return transBinOp(obj, Op.NOT_EQUAL);
  }

  @Override
  protected CirBase visitLessequal(Lessequal obj, String param) {
    return transBinOp(obj, Op.LESS_EQUAL);
  }

  @Override
  protected CirBase visitLess(Less obj, String param) {
    return transBinOp(obj, Op.LESS);
  }

  @Override
  protected CirBase visitGreater(Greater obj, String param) {
    return transBinOp(obj, Op.GREATER);
  }

  @Override
  protected CirBase visitMinus(Minus obj, String param) {
    return transBinOp(obj, Op.MINUS);
  }

  @Override
  protected CirBase visitMod(Mod obj, String param) {
    return transBinOp(obj, Op.MOD);
  }

  @Override
  protected CirBase visitMul(Mul obj, String param) {
    return transBinOp(obj, Op.MUL);
  }

  @Override
  protected CirBase visitDiv(Div obj, String param) {
    return transBinOp(obj, Op.DIV);
  }

  @Override
  protected CirBase visitEqual(Equal obj, String param) {
    return transBinOp(obj, Op.EQUAL);
  }

  @Override
  protected CirBase visitBitAnd(BitAnd obj, String param) {
    return transBinOp(obj, Op.BITAND);
  }

  @Override
  protected CirBase visitBitOr(BitOr obj, String param) {
    return transBinOp(obj, Op.BITOR);
  }

  @Override
  protected CirBase visitLogicOr(LogicOr obj, String param) {
    return transBinOp(obj, Op.LOCOR);
  }

  @Override
  protected CirBase visitShl(Shl obj, String param) {
    return transBinOp(obj, Op.SHL);
  }

  @Override
  protected CirBase visitShr(Shr obj, String param) {
    return transBinOp(obj, Op.SHR);
  }

}
