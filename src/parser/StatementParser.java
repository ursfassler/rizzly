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

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Copy;
import fun.expression.AnyValue;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.statement.Assignment;
import fun.statement.Block;
import fun.statement.CallStmt;
import fun.statement.CaseOpt;
import fun.statement.CaseOptEntry;
import fun.statement.CaseOptRange;
import fun.statement.CaseOptValue;
import fun.statement.CaseStmt;
import fun.statement.ForStmt;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.Return;
import fun.statement.ReturnExpr;
import fun.statement.ReturnVoid;
import fun.statement.Statement;
import fun.statement.VarDefStmt;
import fun.statement.While;
import fun.variable.FuncVariable;

public class StatementParser extends BaseParser {

  public StatementParser(Scanner scanner) {
    super(scanner);
  }

  // EBNF block: { ( return | vardeclstmt | assignment | callstmt | ifstmt | whilestmt | casestmt | forstmt ) }
  protected Block parseBlock() {
    Block res = new Block(peek().getInfo());
    while (true) {
      switch (peek().getType()) {
        case RETURN:
          res.getStatements().add(parseReturn());
          break;
        case IF:
          res.getStatements().add(parseIf());
          break;
        case WHILE:
          res.getStatements().add(parseWhile());
          break;
        case CASE:
          res.getStatements().add(parseCase());
          break;
        case FOR:
          res.getStatements().add(parseFor());
          break;
        case IDENTIFIER:
          res.getStatements().add(parseVardefOrAssignmentOrCallstmt());
          break;
        default:
          return res;
      }
    }
  }

  // EBNF vardefstmt: lhs ":" ref [ "=" expr ] ";"
  private VarDefStmt parseVarDefStmt(FunList<Reference> lhs) {
    ElementInfo info = expect(TokenType.COLON).getInfo();
    Reference type = expr().parseRef();

    Expression initial;
    if (consumeIfEqual(TokenType.EQUAL)) {
      initial = expr().parse();
    } else {
      initial = new AnyValue(info);
    }

    expect(TokenType.SEMI);

    FunList<FuncVariable> variables = new FunList<FuncVariable>();
    for (Reference ref : lhs) {
      if (!ref.getOffset().isEmpty()) {
        RError.err(ErrorType.Error, ref.getInfo(), "expected identifier");
      }

      Reference ntype = Copy.copy(type);
      FuncVariable var = new FuncVariable(ref.getInfo(), ref.getLink().getName(), ntype);
      variables.add(var);
    }

    return new VarDefStmt(info, variables, initial);
  }

  // EBNF casestmt: "case" expression "do" caseopt { caseopt } [ "else" block "end" ] "end"
  private Statement parseCase() {
    Token tok = expect(TokenType.CASE);

    FunList<CaseOpt> optlist = new FunList<CaseOpt>();

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
      block = new Block(tok.getInfo());
    }
    expect(TokenType.END);

    CaseStmt stmt = new CaseStmt(tok.getInfo(), cond, optlist, block);
    return stmt;
  }

  // EBNF caseopt : caseoptval { "," caseoptval } ":" block "end"
  private CaseOpt parseCaseopt() {
    FunList<CaseOptEntry> optval = new FunList<CaseOptEntry>();
    do {
      optval.add(parseCaseoptval());
    } while (consumeIfEqual(TokenType.COMMA));
    ElementInfo info = expect(TokenType.COLON).getInfo();
    Block block = parseBlock();
    expect(TokenType.END);
    return new CaseOpt(info, optval, block);
  }

  // EBNF caseoptval: expr | expr ".." expr
  private CaseOptEntry parseCaseoptval() {
    Expression start = expr().parse();
    if (consumeIfEqual(TokenType.RANGE)) {
      Expression end = expr().parse();
      return new CaseOptRange(start.getInfo(), start, end);
    } else {
      return new CaseOptValue(start.getInfo(), start);
    }
  }

  // EBNF whilestmt: "while" expression "do" block "end"
  private Statement parseWhile() {
    Token tok = expect(TokenType.WHILE);

    Expression cond = expr().parse();
    expect(TokenType.DO);
    Block block = parseBlock();
    expect(TokenType.END);

    While stmt = new While(tok.getInfo(), cond, block);
    return stmt;
  }

  // EBNF forstmt: "for" identifier "in" typeref "do" block "end"
  private Statement parseFor() {
    Token tok = expect(TokenType.FOR);

    String name = expect(TokenType.IDENTIFIER).getData();
    expect(TokenType.IN);
    Reference type = expr().parseRef();
    expect(TokenType.DO);
    Block block = parseBlock();
    expect(TokenType.END);

    FuncVariable var = new FuncVariable(tok.getInfo(), name, type);

    RError.err(ErrorType.Warning, tok.getInfo(), "for loop is very experimental");

    return new ForStmt(tok.getInfo(), var, block);
  }

  // EBNF ifstmt: "if" expression "then" block { "ef" expression "then" block } [ "else" block ] "end"
  private Statement parseIf() {
    Token tok = expect(TokenType.IF);

    IfStmt stmt = new IfStmt(tok.getInfo());

    {
      Expression expr = expr().parse();
      expect(TokenType.THEN);
      Block block = parseBlock();
      stmt.addOption(new IfOption(expr.getInfo(), expr, block));
    }

    while (consumeIfEqual(TokenType.EF)) {
      Expression expr = expr().parse();
      expect(TokenType.THEN);
      Block block = parseBlock();
      stmt.addOption(new IfOption(expr.getInfo(), expr, block));
    }

    while (consumeIfEqual(TokenType.ELSE)) {
      Block block = parseBlock();
      stmt.setDefblock(block);
    }

    expect(TokenType.END);

    return stmt;
  }

  // EBNF return: "return" [ expression ] ";"
  private Statement parseReturn() {
    Token tok = expect(TokenType.RETURN);
    Return ret;
    if (peek().getType() != TokenType.SEMI) {
      ret = new ReturnExpr(tok.getInfo(), expr().parse());
    } else {
      ret = new ReturnVoid(tok.getInfo());
    }
    expect(TokenType.SEMI);
    return ret;
  }

  private Statement parseVardefOrAssignmentOrCallstmt() {
    FunList<Reference> lhs = parseLhs();
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
        return new CallStmt(tok.getInfo(), lhs.get(0));
      }
      default: {
        error.RError.err(ErrorType.Fatal, peek().getInfo(), "Unexpected token: " + tok.getType());
        return null;
      }
    }
  }

  // EBNF lhs: varref { "," varref }
  private FunList<Reference> parseLhs() {
    FunList<Reference> lhs = new FunList<Reference>();
    do {
      lhs.add(expr().parseRef());
    } while (consumeIfEqual(TokenType.COMMA));
    return lhs;
  }

  // EBNF assignment: lhs ":=" expr ";"
  private Assignment parseAssignment(FunList<Reference> ref) {
    Token tok = expect(TokenType.BECOMES);
    Expression rhs = expr().parse();
    expect(TokenType.SEMI);
    return new Assignment(tok.getInfo(), ref, rhs);
  }

}
