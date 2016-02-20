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

package parser;

import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.copy.Copy;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.expression.value.AnyValue;
import ast.data.reference.Reference;
import ast.data.reference.ReferenceOffset;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.CaseOpt;
import ast.data.statement.CaseOptEntry;
import ast.data.statement.CaseOptRange;
import ast.data.statement.CaseOptValue;
import ast.data.statement.CaseStmt;
import ast.data.statement.ExpressionReturn;
import ast.data.statement.ForStmt;
import ast.data.statement.IfOption;
import ast.data.statement.IfStatement;
import ast.data.statement.MultiAssignment;
import ast.data.statement.Statement;
import ast.data.statement.VarDefInitStmt;
import ast.data.statement.VoidReturn;
import ast.data.statement.WhileStmt;
import ast.data.variable.FunctionVariable;
import ast.meta.MetaList;
import error.ErrorType;
import error.RError;

public class StatementParser extends BaseParser {

  public StatementParser(PeekNReader<Token> scanner) {
    super(scanner);
  }

  // EBNF block: { ( return | vardeclstmt | assignment | callstmt | ifstmt |
  // whilestmt | casestmt | forstmt ) }
  public Block parseBlock() {
    Block res = new Block();
    res.metadata().add(peek().getMetadata());
    while (true) {
      switch (peek().getType()) {
        case RETURN:
          res.statements.add(parseReturn());
          break;
        case IF:
          res.statements.add(parseIf());
          break;
        case WHILE:
          res.statements.add(parseWhile());
          break;
        case CASE:
          res.statements.add(parseCase());
          break;
        case FOR:
          res.statements.add(parseFor());
          break;
        case IDENTIFIER:
          res.statements.add(parseVardefOrAssignmentOrCallstmt());
          break;
        default:
          return res;
      }
    }
  }

  // EBNF vardefstmt: lhs ":" ref [ "=" expr ] ";"
  private VarDefInitStmt parseVarDefStmt(AstList<Reference> lhs) {
    MetaList info = expect(TokenType.COLON).getMetadata();
    Reference type = expr().parseRefType();

    Expression initial;
    if (consumeIfEqual(TokenType.EQUAL)) {
      initial = expr().parse();
    } else {
      initial = new AnyValue();
      initial.metadata().add(info);
    }

    expect(TokenType.SEMI);

    AstList<FunctionVariable> variables = new AstList<FunctionVariable>();
    for (Reference ref : lhs) {
      if (!((ReferenceOffset) ref).getOffset().isEmpty()) {
        RError.err(ErrorType.Error, "expected identifier", ref.metadata());
      }

      Reference ntype = Copy.copy(type);
      FunctionVariable var = new FunctionVariable(ref.getAnchor().targetName(), ntype);
      var.metadata().add(ref.metadata());
      variables.add(var);
    }

    VarDefInitStmt stmt = new VarDefInitStmt(variables, initial);
    stmt.metadata().add(info);
    return stmt;
  }

  // EBNF casestmt: "case" expression "of" caseopt { caseopt } [ "else" block
  // "end" ] "end"
  private Statement parseCase() {
    Token tok = expect(TokenType.CASE);

    AstList<CaseOpt> optlist = new AstList<CaseOpt>();

    Expression cond = expr().parse();
    expect(TokenType.OF);

    do {
      CaseOpt opt = parseCaseopt();
      optlist.add(opt);
    } while ((peek().getType() != TokenType.ELSE) && (peek().getType() != TokenType.END));
    Block block;
    if (consumeIfEqual(TokenType.ELSE)) {
      block = parseBlock();
      expect(TokenType.END);
    } else {
      block = new Block();
      block.metadata().add(tok.getMetadata());
    }
    expect(TokenType.END);

    CaseStmt stmt = new CaseStmt(cond, optlist, block);
    stmt.metadata().add(tok.getMetadata());
    return stmt;
  }

  // EBNF caseopt : caseoptval { "," caseoptval } ":" block "end"
  private CaseOpt parseCaseopt() {
    AstList<CaseOptEntry> optval = new AstList<CaseOptEntry>();
    do {
      optval.add(parseCaseoptval());
    } while (consumeIfEqual(TokenType.COMMA));
    MetaList info = expect(TokenType.COLON).getMetadata();
    Block block = parseBlock();
    expect(TokenType.END);
    CaseOpt caseOpt = new CaseOpt(optval, block);
    caseOpt.metadata().add(info);
    return caseOpt;
  }

  // EBNF caseoptval: expr | expr ".." expr
  private CaseOptEntry parseCaseoptval() {
    Expression start = expr().parse();
    if (consumeIfEqual(TokenType.RANGE)) {
      Expression end = expr().parse();
      return new CaseOptRange(start.metadata(), start, end);
    } else {
      return new CaseOptValue(start.metadata(), start);
    }
  }

  // EBNF whilestmt: "while" expression "do" block "end"
  private Statement parseWhile() {
    Token tok = expect(TokenType.WHILE);

    Expression cond = expr().parse();
    expect(TokenType.DO);
    Block block = parseBlock();
    expect(TokenType.END);

    WhileStmt stmt = new WhileStmt(cond, block);
    stmt.metadata().add(tok.getMetadata());
    return stmt;
  }

  // EBNF forstmt: "for" identifier "in" typeref "do" block "end"
  private Statement parseFor() {
    Token tok = expect(TokenType.FOR);

    String name = expect(TokenType.IDENTIFIER).getData();
    expect(TokenType.IN);
    Reference type = expr().parseRefType();
    expect(TokenType.DO);
    Block block = parseBlock();
    expect(TokenType.END);

    FunctionVariable var = new FunctionVariable(name, type);
    var.metadata().add(tok.getMetadata());

    RError.err(ErrorType.Warning, "for loop is very experimental", tok.getMetadata());

    ForStmt stmt = new ForStmt(var, block);
    stmt.metadata().add(tok.getMetadata());
    return stmt;
  }

  // EBNF ifstmt: "if" expression "then" block { "ef" expression "then" block }
  // [ "else" block ] "end"
  private Statement parseIf() {
    Token tok = expect(TokenType.IF);

    IfStatement stmt = new IfStatement();
    stmt.metadata().add(tok.getMetadata());

    {
      Expression expr = expr().parse();
      expect(TokenType.THEN);
      Block block = parseBlock();
      IfOption ifopt = new IfOption(expr, block);
      ifopt.metadata().add(expr.metadata());
      stmt.option.add(ifopt);
    }

    while (consumeIfEqual(TokenType.EF)) {
      Expression expr = expr().parse();
      expect(TokenType.THEN);
      Block block = parseBlock();
      IfOption ifopt = new IfOption(expr, block);
      ifopt.metadata().add(expr.metadata());
      stmt.option.add(ifopt);
    }

    while (consumeIfEqual(TokenType.ELSE)) {
      Block block = parseBlock();
      stmt.defblock = block;
    }

    expect(TokenType.END);

    return stmt;
  }

  // EBNF return: "return" [ expression ] ";"
  private Statement parseReturn() {
    Token tok = expect(TokenType.RETURN);
    ast.data.statement.Return ret;
    if (peek().getType() != TokenType.SEMI) {
      ret = new ExpressionReturn(expr().parse());
    } else {
      ret = new VoidReturn();
    }
    ret.metadata().add(tok.getMetadata());
    expect(TokenType.SEMI);
    return ret;
  }

  private Statement parseVardefOrAssignmentOrCallstmt() {
    AstList<Reference> lhs = parseLhs();
    Token tok = peek();
    switch (tok.getType()) {
      case COLON: {
        return parseVarDefStmt(lhs);
      }
      case BECOMES: {
        return parseAssignment(lhs);
      }
      case SEMI: {
        assert (lhs.size() == 1);
        tok = next();
        CallStmt stmt = new CallStmt(lhs.get(0));
        stmt.metadata().add(tok.getMetadata());
        return stmt;
      }
      default: {
        error.RError.err(ErrorType.Fatal, "Unexpected token: " + tok.getType(), peek().getMetadata());
        return null;
      }
    }
  }

  // EBNF lhs: varref { "," varref }
  private AstList<Reference> parseLhs() {
    AstList<Reference> lhs = new AstList<Reference>();
    do {
      lhs.add(expr().parseRef());
    } while (consumeIfEqual(TokenType.COMMA));
    return lhs;
  }

  // EBNF assignment: lhs ":=" expr ";"
  private MultiAssignment parseAssignment(AstList<Reference> ref) {
    Token tok = expect(TokenType.BECOMES);
    Expression rhs = expr().parse();
    expect(TokenType.SEMI);
    MultiAssignment stmt = new MultiAssignment(ref, rhs);
    stmt.metadata().add(tok.getMetadata());
    return stmt;
  }

}
