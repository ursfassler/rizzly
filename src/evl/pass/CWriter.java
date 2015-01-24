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

import evl.Evl;
import evl.NullTraverser;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.NamedElementValue;
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
import evl.expression.binop.Relation;
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
import evl.expression.unop.UnaryExp;
import evl.function.Function;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Named;
import evl.other.Namespace;
import evl.statement.Assignment;
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
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.type.out.SIntType;
import evl.type.out.UIntType;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

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
  protected Void visitNumber(evl.expression.Number obj, Boolean param) {
    sw.wr(obj.getValue().toString());
    return null;
  }

  private Void relation(Relation obj, String op, Boolean param) {
    sw.wr("(");
    visit(obj.getLeft(), param);
    sw.wr(" ");
    sw.wr(op);
    sw.wr(" ");
    visit(obj.getRight(), param);
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
    visit(obj.getLeft(), param);
    sw.wr(" ");
    sw.wr(exp);
    sw.wr(" ");
    visit(obj.getRight(), param);
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
    visit(obj.getExpr(), param);
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
    sw.wr(name(obj.getLink()));
    visitList(obj.getOffset(), param);
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, Boolean param) {
    sw.wr("(");
    for (int i = 0; i < obj.getActualParameter().size(); i++) {
      if (i > 0) {
        sw.wr(",");
      }
      visit(obj.getActualParameter().get(i), param);
    }
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, Boolean param) {
    sw.wr(".");
    sw.wr(obj.getName());
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, Boolean param) {
    sw.wr(".");
    sw.wr(CWriter.ARRAY_DATA_NAME);
    sw.wr("[");
    visit(obj.getIndex(), param);
    sw.wr("]");
    return null;
  }

  protected void writeFuncHeader(Function obj) {
    visit(obj.getRet(), true);
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr("(");
    if (obj.getParam().isEmpty()) {
      sw.wr("void");
    } else {
      for (int i = 0; i < obj.getParam().size(); i++) {
        if (i > 0) {
          sw.wr(",");
        }
        Variable var = obj.getParam().get(i);
        visit(var, true);
      }
    }
    sw.wr(")");
  }

  @Override
  protected Void visitFunctionImpl(Function obj, Boolean param) {
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
        visit(obj.getBody(), param);
      } else {
        sw.wr(";");
      }
      sw.nl();
    } else {
      sw.wr("static ");
      writeFuncHeader(obj);
      if (param) {
        sw.nl();
        visit(obj.getBody(), param);
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
    visit(obj.getType(), param);
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(" = ");
    visit(obj.getDef(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Boolean param) {
    sw.wr("static ");
    visit(obj.getType(), param);
    sw.wr(" ");
    sw.wr(name(obj));
    sw.wr(" = ");
    visit(obj.getDef(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Boolean param) {
    visit(obj.getType(), param);
    sw.wr(" ");
    sw.wr(name(obj));
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, Boolean param) {
    visit(obj.getCall(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Boolean param) {
    visit(obj.getLeft(), param);
    sw.wr(" = ");
    visit(obj.getRight(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, Boolean param) {
    sw.wr("{");
    sw.nl();
    sw.incIndent();
    visitList(obj.getStatements(), param);
    sw.decIndent();
    sw.wr("}");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitRangeType(RangeType obj, Boolean param) {
    sw.wr("\"" + obj.getName() + "\"");
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitSIntType(SIntType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("int" + obj.getBytes() * 8 + "_t ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitUIntType(UIntType obj, Boolean param) {
    sw.wr("typedef ");
    sw.wr("uint" + obj.getBytes() * 8 + "_t ");
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
    visit(obj.getType(), param);
    sw.wr(" ");
    sw.wr(CWriter.ARRAY_DATA_NAME);
    sw.wr("[");
    sw.wr(obj.getSize().toString());
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
    visitList(obj.getElement(), param);
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

    visit(obj.getTag(), param);

    sw.wr("union {");
    sw.nl();
    sw.incIndent();
    visitList(obj.getElement(), param);
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
    visitList(obj.getElement(), param);
    sw.decIndent();
    sw.wr("} ");
    sw.wr(name(obj));
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Boolean param) {
    visit(obj.getRef(), param);
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
    visit(obj.getExpr(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitIfStmt(IfStmt obj, Boolean param) {
    boolean first = true;

    for (IfOption opt : obj.getOption()) {
      if (!first) {
        sw.wr(" else ");
      } else {
        first = false;
      }
      sw.wr("if( ");
      visit(opt.getCondition(), param);
      sw.wr(" )");
      visit(opt.getCode(), param);
    }

    sw.wr(" else ");
    visit(obj.getDefblock(), param);

    return null;
  }

  @Override
  protected Void visitWhileStmt(WhileStmt obj, Boolean param) {
    sw.wr("while( ");
    visit(obj.getCondition(), param);
    sw.wr(" )");
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Boolean param) {
    visit(obj.getVariable(), param);
    sw.wr(";");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitCaseStmt(CaseStmt obj, Boolean param) {
    sw.wr("switch( ");
    visit(obj.getCondition(), param);
    sw.wr(" ){");
    sw.nl();
    sw.incIndent();
    visitList(obj.getOption(), param);

    sw.wr("default:{");
    sw.nl();
    sw.incIndent();
    visit(obj.getOtherwise(), param);
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
    visitList(obj.getValue(), param);
    sw.wr("{");
    sw.nl();
    sw.incIndent();

    visit(obj.getCode(), param);
    sw.wr("break;");
    sw.nl();

    sw.decIndent();
    sw.wr("}");
    sw.nl();
    return null;
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Boolean param) {
    evl.expression.Number numStart = (evl.expression.Number) obj.getStart();
    evl.expression.Number numEnd = (evl.expression.Number) obj.getEnd();
    sw.wr("case ");
    sw.wr(numStart.getValue().toString());
    sw.wr(" ... ");
    sw.wr(numEnd.getValue().toString());
    sw.wr(": ");
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Boolean param) {
    evl.expression.Number num = (evl.expression.Number) obj.getValue();
    sw.wr("case ");
    sw.wr(num.getValue().toString());
    sw.wr(": ");
    return null;
  }

  @Override
  protected Void visitStringValue(StringValue obj, Boolean param) {
    sw.wr("\"");
    sw.wr(escape(obj.getValue()));
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
    for (int i = 0; i < obj.getValue().size(); i++) {
      if (i > 0) {
        sw.wr(",");
      }
      visit(obj.getValue().get(i), param);
    }
    sw.wr("} ");
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitNamedElementValue(NamedElementValue obj, Boolean param) {
    sw.wr(".");
    sw.wr(obj.getName());
    sw.wr("=");
    visit(obj.getValue(), param);
    sw.wr(",");
    return null;
  }

  @Override
  protected Void visitUnsafeUnionValue(UnsafeUnionValue obj, Boolean param) {
    sw.wr("{");
    visit(obj.getContentValue(), param);
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitUnionValue(UnionValue obj, Boolean param) {
    sw.wr("{");
    visit(obj.getTagValue(), param);
    // sw.wr(",");
    visit(obj.getContentValue(), param);
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitRecordValue(RecordValue obj, Boolean param) {
    sw.wr("{");
    visitList(obj.getValue(), param);
    sw.wr("}");
    return null;
  }

  @Override
  protected Void visitTypeRef(SimpleRef obj, Boolean param) {
    sw.wr(name(obj.getLink()));
    return null;
  }

  @Override
  protected Void visitTypeCast(TypeCast obj, Boolean param) {
    sw.wr("((");
    visit(obj.getCast(), param);
    sw.wr(")");
    visit(obj.getValue(), param);
    sw.wr(")");
    return null;
  }

  @Override
  protected Void visitBoolValue(BoolValue obj, Boolean param) {
    if (obj.isValue()) {
      sw.wr("true");
    } else {
      sw.wr("false");
    }
    return null;
  }

}
