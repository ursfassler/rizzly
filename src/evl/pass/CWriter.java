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

package evl.pass;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import pass.EvlPass;
import util.StreamWriter;

import common.Property;

import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.expression.ArrayValue;
import evl.data.expression.BoolValue;
import evl.data.expression.NamedValue;
import evl.data.expression.RecordValue;
import evl.data.expression.StringValue;
import evl.data.expression.TypeCast;
import evl.data.expression.UnionValue;
import evl.data.expression.UnsafeUnionValue;
import evl.data.expression.binop.BinaryExp;
import evl.data.expression.binop.BitAnd;
import evl.data.expression.binop.BitOr;
import evl.data.expression.binop.BitXor;
import evl.data.expression.binop.Div;
import evl.data.expression.binop.Equal;
import evl.data.expression.binop.Greater;
import evl.data.expression.binop.Greaterequal;
import evl.data.expression.binop.Less;
import evl.data.expression.binop.Lessequal;
import evl.data.expression.binop.LogicAnd;
import evl.data.expression.binop.LogicOr;
import evl.data.expression.binop.Minus;
import evl.data.expression.binop.Mod;
import evl.data.expression.binop.Mul;
import evl.data.expression.binop.Notequal;
import evl.data.expression.binop.Plus;
import evl.data.expression.binop.Relation;
import evl.data.expression.binop.Shl;
import evl.data.expression.binop.Shr;
import evl.data.expression.reference.RefCall;
import evl.data.expression.reference.RefIndex;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.expression.unop.BitNot;
import evl.data.expression.unop.LogicNot;
import evl.data.expression.unop.Uminus;
import evl.data.expression.unop.UnaryExp;
import evl.data.function.Function;
import evl.data.function.ret.FuncReturnNone;
import evl.data.function.ret.FuncReturnType;
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
import evl.data.statement.VarDefStmt;
import evl.data.statement.WhileStmt;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.StringType;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.type.composed.UnionType;
import evl.data.type.composed.UnsafeUnionType;
import evl.data.type.out.AliasType;
import evl.data.type.out.SIntType;
import evl.data.type.out.UIntType;
import evl.data.type.special.VoidType;
import evl.data.variable.Constant;
import evl.data.variable.FuncVariable;
import evl.data.variable.StateVariable;
import evl.data.variable.Variable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;

public class CWriter extends EvlPass {
  public static final String ARRAY_DATA_NAME = "data";

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    String cfile = kb.getOutDir() + evl.getName() + ".c";
    try {
      CWriterWorker printer = new CWriterWorker(new StreamWriter(new PrintStream(cfile)));
      printer.traverse(evl, null);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

}

class CWriterWorker extends NullTraverser<Void, Boolean> {
  final private StreamWriter sw;

  public CWriterWorker(StreamWriter sw) {
    super();
    this.sw = sw;
  }

  @Deprecated
  private String name(Named obj) {
    return obj.getName();
  }

  @Override
  protected Void visitDefault(Evl obj, Boolean param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Boolean param) {
    EvlList<Type> types = obj.getItems(Type.class, false);
    EvlList<Function> functions = obj.getItems(Function.class, false);
    EvlList<Variable> variables = obj.getItems(Variable.class, false);

    assert (types.size() + functions.size() + variables.size() == obj.getChildren().size());

    sw.wr("#include <stdint.h>");
    sw.nl();
    sw.wr("#include <stdbool.h>");
    sw.nl();
    sw.nl();

    visitList(types, true);
    sw.nl();
    visitList(functions, false);
    sw.nl();
    visitList(variables, true);
    sw.nl();
    visitList(functions, true);
    sw.nl();

    return null;
  }

  @Override
  protected Void visitNumber(evl.data.expression.Number obj, Boolean param) {
    sw.wr(obj.value.toString());
    return null;
  }

  private Void relation(Relation obj, String op, Boolean param) {
    sw.wr("(");
    visit(obj.left, param);
    sw.wr(" ");
    sw.wr(op);
    sw.wr(" ");
    visit(obj.right, param);
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitGreater(Greater obj, Boolean param) {
    return relation(obj, ">", param);
  }

  @Override
  protected Void visitGreaterequal(Greaterequal obj, Boolean param) {
    return relation(obj, ">=", param);
  }

  @Override
  protected Void visitLess(Less obj, Boolean param) {
    return relation(obj, "<", param);
  }

  @Override
  protected Void visitLessequal(Lessequal obj, Boolean param) {
    return relation(obj, "<=", param);
  }

  @Override
  protected Void visitEqual(Equal obj, Boolean param) {
    return relation(obj, "==", param);
  }

  @Override
  protected Void visitNotequal(Notequal obj, Boolean param) {
    return relation(obj, "!=", param);
  }

  private Void binexp(BinaryExp obj, String exp, Boolean param) {
    sw.wr("(");
    visit(obj.left, param);
    sw.wr(" ");
    sw.wr(exp);
    sw.wr(" ");
    visit(obj.right, param);
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitMod(Mod obj, Boolean param) {
    return binexp(obj, "%", param);
  }

  @Override
  protected Void visitMinus(Minus obj, Boolean param) {
    return binexp(obj, "-", param);
  }

  @Override
  protected Void visitPlus(Plus obj, Boolean param) {
    return binexp(obj, "+", param);
  }

  @Override
  protected Void visitLogicOr(LogicOr obj, Boolean param) {
    return binexp(obj, "||", param);
  }

  @Override
  protected Void visitDiv(Div obj, Boolean param) {
    return binexp(obj, "/", param);
  }

  @Override
  protected Void visitMul(Mul obj, Boolean param) {
    return binexp(obj, "*", param);
  }

  @Override
  protected Void visitLogicAnd(LogicAnd obj, Boolean param) {
    return binexp(obj, "&&", param);
  }

  @Override
  protected Void visitBitAnd(BitAnd obj, Boolean param) {
    return binexp(obj, "&", param);
  }

  @Override
  protected Void visitBitOr(BitOr obj, Boolean param) {
    return binexp(obj, "|", param);
  }

  @Override
  protected Void visitBitXor(BitXor obj, Boolean param) {
    return binexp(obj, "^", param);
  }

  @Override
  protected Void visitShl(Shl obj, Boolean param) {
    return binexp(obj, "<<", param);
  }

  @Override
  protected Void visitShr(Shr obj, Boolean param) {
    return binexp(obj, ">>", param);
  }

  protected Void unexp(UnaryExp obj, String exp, Boolean param) {
    sw.wr("(");
    sw.wr(exp);
    sw.wr(" ");
    visit(obj.expr, param);
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitLogicNot(LogicNot obj, Boolean param) {
    return unexp(obj, "!", param);
  }

  @Override
  protected Void visitBitNot(BitNot obj, Boolean param) {
    return unexp(obj, "~", param);
  }

  @Override
  protected Void visitUminus(Uminus obj, Boolean param) {
    return unexp(obj, "-", param);
  }

  @Override
  protected Void visitReference(Reference obj, Boolean param) {
    sw.wr(name(obj.link));
    visitList(obj.offset, param);
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, Boolean param) {
    sw.wr("(");
    for (int i = 0; i < obj.actualParameter.value.size(); i++) {
      if (i > 0) {
        sw.wr(",");
      }
      visit(obj.actualParameter.value.get(i), param);
    }
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, Boolean param) {
    sw.wr(".");
    sw.wr(obj.name);
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, Boolean param) {
    sw.wr(".");
    sw.wr(CWriter.ARRAY_DATA_NAME);
    sw.wr("[");
    visit(obj.index, param);
    sw.wr("]");
    return null;
  }

  @Override
  protected Void visitFuncReturnType(FuncReturnType obj, Boolean param) {
    visit(obj.type, param);
    return null;
  }

  @Override
  protected Void visitFuncReturnNone(FuncReturnNone obj, Boolean param) {
    sw.wr("void");
    return null;
  }

  protected void writeFuncHeader(Function obj) {
    visit(obj.ret, true);
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr("(");
    if (obj.param.isEmpty()) {
      sw.wr("void");
    } else {
      for (int i = 0; i < obj.param.size(); i++) {
        if (i > 0) {
          sw.wr(",");
        }
        Variable var = obj.param.get(i);
        visit(var, true);
      }
    }
    sw.wr(")");
  }

  @Override
  protected Void visitFunction(Function obj, Boolean param) {
    if (obj.properties().containsKey(Property.Extern)) {
      if (!param) {
        sw.wr("extern ");
        writeFuncHeader(obj);
        sw.wr(";");
        sw.nl();
      }
    } else if (obj.properties().containsKey(Property.Public)) {
      writeFuncHeader(obj);
      if (param) {
        sw.nl();
        visit(obj.body, param);
      } else {
        sw.wr(";");
      }
      sw.nl();
    } else {
      sw.wr("static ");
      writeFuncHeader(obj);
      if (param) {
        sw.nl();
        visit(obj.body, param);
      } else {
        sw.wr(";");
      }
      sw.nl();
    }
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Boolean param) {
    sw.wr("static const ");
    visit(obj.type, param);
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(" = ");
    visit(obj.def, param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Boolean param) {
    sw.wr("static ");
    visit(obj.type, param);
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(" = ");
    visit(obj.def, param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Boolean param) {
    visit(obj.type, param);
    sw.wr(" ");
    sw.wr(name(obj));
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Boolean param) {
    visit(obj.call, param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitAssignmentSingle(AssignmentSingle obj, Boolean param) {
    visit(obj.left, param);
    sw.wr(" = ");
    visit(obj.right, param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, Boolean param) {
    sw.wr("{");
    sw.nl();
    sw.incIndent();
    visitList(obj.statements, param);
    sw.decIndent();
    sw.wr("}");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitAliasType(AliasType obj, Boolean param) {
    sw.wr("typedef ");
    visit(obj.ref, param);
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitSIntType(SIntType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("int" + obj.bytes * 8 + "_t ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitUIntType(UIntType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("uint" + obj.bytes * 8 + "_t ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitArrayType(ArrayType obj, Boolean param) {
    sw.wr("typedef struct {");
    sw.nl();
    sw.incIndent();
    visit(obj.type, param);
    sw.wr(" ");
    sw.wr(CWriter.ARRAY_DATA_NAME);
    sw.wr("[");
    sw.wr(obj.size.toString());
    sw.wr("]");
    sw.wr(";");
    sw.decIndent();
    sw.nl();
    sw.wr("} ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("void ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitBooleanType(BooleanType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("bool");
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("char*");
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitUnsafeUnionType(UnsafeUnionType obj, Boolean param) {
    sw.wr("typedef union {");
    sw.nl();
    sw.incIndent();
    visitList(obj.element, param);
    sw.decIndent();
    sw.wr("} ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, Boolean param) {
    sw.wr("typedef struct {");
    sw.nl();
    sw.incIndent();

    visit(obj.tag, param);

    sw.wr("union {");
    sw.nl();
    sw.incIndent();
    visitList(obj.element, param);
    sw.decIndent();
    sw.wr("};");
    sw.nl();

    sw.decIndent();
    sw.wr("} ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, Boolean param) {
    sw.wr("typedef struct {");
    sw.nl();
    sw.incIndent();
    visitList(obj.element, param);
    sw.decIndent();
    sw.wr("} ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Boolean param) {
    visit(obj.ref, param);
    sw.wr(" ");
    sw.wr(obj.getName());
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitReturnVoid(ReturnVoid obj, Boolean param) {
    sw.wr("return;");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitReturnExpr(ReturnExpr obj, Boolean param) {
    sw.wr("return ");
    visit(obj.expr, param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, Boolean param) {
    boolean first = true;

    for (IfOption opt : obj.option) {
      if (!first) {
        sw.wr(" else ");
      } else {
        first = false;
      }
      sw.wr("if( ");
      visit(opt.condition, param);
      sw.wr(" )");
      visit(opt.code, param);
    }

    sw.wr(" else ");
    visit(obj.defblock, param);

    return null;
  }

  @Override
  protected Void visitWhileStmt(WhileStmt obj, Boolean param) {
    sw.wr("while( ");
    visit(obj.condition, param);
    sw.wr(" )");
    visit(obj.body, param);
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Boolean param) {
    visit(obj.variable, param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, Boolean param) {
    sw.wr("switch( ");
    visit(obj.condition, param);
    sw.wr(" ){");
    sw.nl();
    sw.incIndent();
    visitList(obj.option, param);

    sw.wr("default:{");
    sw.nl();
    sw.incIndent();
    visit(obj.otherwise, param);
    sw.wr("break;");
    sw.nl();
    sw.decIndent();
    sw.wr("}");
    sw.nl();

    sw.decIndent();
    sw.wr("}");
    sw.nl();

    return null;
  }

  @Override
  protected Void visitCaseOpt(CaseOpt obj, Boolean param) {
    visitList(obj.value, param);
    sw.wr("{");
    sw.nl();
    sw.incIndent();

    visit(obj.code, param);
    sw.wr("break;");
    sw.nl();

    sw.decIndent();
    sw.wr("}");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Boolean param) {
    evl.data.expression.Number numStart = (evl.data.expression.Number) obj.start;
    evl.data.expression.Number numEnd = (evl.data.expression.Number) obj.end;
    sw.wr("case ");
    sw.wr(numStart.value.toString());
    sw.wr(" ... ");
    sw.wr(numEnd.value.toString());
    sw.wr(": ");
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Boolean param) {
    evl.data.expression.Number num = (evl.data.expression.Number) obj.value;
    sw.wr("case ");
    sw.wr(num.value.toString());
    sw.wr(": ");
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, Boolean param) {
    sw.wr("\"");
    sw.wr(escape(obj.value));
    sw.wr("\"");
    return null;
  }

  private String escape(String value) {
    String ret = "";

    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '\"':
          ret += "\\\"";
          break;
        case '\n':
          ret += "\\n";
          break;
        case '\t':
          ret += "\\t";
          break;
        // TODO more symbols to escape?
        default:
          ret += c;
          break;
      }
    }

    return ret;
  }

  @Override
  protected Void visitArrayValue(ArrayValue obj, Boolean param) {
    sw.wr("{");
    sw.wr(" ." + CWriter.ARRAY_DATA_NAME + " = {");
    for (int i = 0; i < obj.value.size(); i++) {
      if (i > 0) {
        sw.wr(",");
      }
      visit(obj.value.get(i), param);
    }
    sw.wr("} ");
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitNamedValue(NamedValue obj, Boolean param) {
    sw.wr(".");
    sw.wr(obj.name);
    sw.wr("=");
    visit(obj.value, param);
    sw.wr(",");
    return null;
  }

  @Override
  protected Void visitUnsafeUnionValue(UnsafeUnionValue obj, Boolean param) {
    sw.wr("{");
    visit(obj.contentValue, param);
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitUnionValue(UnionValue obj, Boolean param) {
    sw.wr("{");
    visit(obj.tagValue, param);
    // sw.wr(",");
    visit(obj.contentValue, param);
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitRecordValue(RecordValue obj, Boolean param) {
    sw.wr("{");
    visitList(obj.value, param);
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitSimpleRef(SimpleRef obj, Boolean param) {
    sw.wr(name(obj.link));
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, Boolean param) {
    sw.wr("((");
    visit(obj.cast, param);
    sw.wr(")");
    visit(obj.value, param);
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, Boolean param) {
    if (obj.value) {
      sw.wr("true");
    } else {
      sw.wr("false");
    }
    return null;
  }

}
