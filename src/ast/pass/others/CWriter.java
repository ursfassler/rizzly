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

package ast.pass.others;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.expression.RefExp;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.BinaryExp;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.binop.BitOr;
import ast.data.expression.binop.BitXor;
import ast.data.expression.binop.Div;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.Greaterequal;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.Lessequal;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Mod;
import ast.data.expression.binop.Mul;
import ast.data.expression.binop.Notequal;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Relation;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.unop.BitNot;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.unop.Uminus;
import ast.data.expression.unop.UnaryExp;
import ast.data.expression.value.ArrayValue;
import ast.data.expression.value.BoolValue;
import ast.data.expression.value.NamedValue;
import ast.data.expression.value.RecordValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.UnionValue;
import ast.data.expression.value.UnsafeUnionValue;
import ast.data.function.Function;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnType;
import ast.data.reference.RefCall;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.Reference;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptRange;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStmt;
import ast.data.statement.ReturnExpr;
import ast.data.statement.ReturnVoid;
import ast.data.statement.VarDefStmt;
import ast.data.statement.WhileStmt;
import ast.data.type.Type;
import ast.data.type.TypeRef;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.StringType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.type.out.AliasType;
import ast.data.type.out.SIntType;
import ast.data.type.out.UIntType;
import ast.data.type.special.VoidType;
import ast.data.variable.Constant;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.Variable;
import ast.dispatcher.NullDispatcher;
import ast.doc.StreamWriter;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.TypeFilter;

public class CWriter extends AstPass {
  public static final String ARRAY_DATA_NAME = "data";

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    String cfile = kb.getOutDir() + ast.name + ".c";
    try {
      CWriterWorker printer = new CWriterWorker(new StreamWriter(new PrintStream(cfile)));
      printer.traverse(ast, null);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

}

class CWriterWorker extends NullDispatcher<Void, Boolean> {
  final private StreamWriter sw;

  public CWriterWorker(StreamWriter sw) {
    super();
    this.sw = sw;
  }

  @Deprecated
  private String name(Named obj) {
    return obj.name;
  }

  @Override
  protected Void visitDefault(Ast obj, Boolean param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Boolean param) {
    AstList<Type> types = TypeFilter.select(obj.children, Type.class);
    AstList<Function> functions = TypeFilter.select(obj.children, Function.class);
    AstList<Variable> variables = TypeFilter.select(obj.children, Variable.class);

    assert (types.size() + functions.size() + variables.size() == obj.children.size());

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
  protected Void visitNumber(ast.data.expression.value.NumberValue obj, Boolean param) {
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
  protected Void visitRefExpr(RefExp obj, Boolean param) {
    visit(obj.ref, param);
    return null;
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
    switch (obj.property) {
      case External:
        if (!param) {
          sw.wr("extern ");
          writeFuncHeader(obj);
          sw.wr(";");
          sw.nl();
        }
        break;
      case Public:
        writeFuncHeader(obj);
        if (param) {
          sw.nl();
          visit(obj.body, param);
        } else {
          sw.wr(";");
        }
        sw.nl();
        break;
      case Private:
        sw.wr("static ");
        writeFuncHeader(obj);
        if (param) {
          sw.nl();
          visit(obj.body, param);
        } else {
          sw.wr(";");
        }
        sw.nl();
        break;
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
    visit(obj.typeref, param);
    sw.wr(" ");
    sw.wr(obj.name);
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
    ast.data.expression.value.NumberValue numStart = (ast.data.expression.value.NumberValue) obj.start;
    ast.data.expression.value.NumberValue numEnd = (ast.data.expression.value.NumberValue) obj.end;
    sw.wr("case ");
    sw.wr(numStart.value.toString());
    sw.wr(" ... ");
    sw.wr(numEnd.value.toString());
    sw.wr(": ");
    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Boolean param) {
    ast.data.expression.value.NumberValue num = (ast.data.expression.value.NumberValue) obj.value;
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
  protected Void visitTypeRef(TypeRef obj, Boolean param) {
    visit(obj.ref, param);
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
