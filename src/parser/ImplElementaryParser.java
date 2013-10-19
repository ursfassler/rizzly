package parser;

import java.util.List;

import util.Pair;

import common.ElementInfo;

import fun.expression.reference.ReferenceLinked;
import fun.function.FunctionHeader;
import fun.function.impl.FuncEntryExit;
import fun.hfsm.State;
import fun.other.Component;
import fun.other.ImplElementary;
import fun.statement.Block;
import fun.variable.ConstPrivate;
import fun.variable.StateVariable;

public class ImplElementaryParser extends ImplBaseParser {

  public ImplElementaryParser(Scanner scanner) {
    super(scanner);
  }

  public static Component parse(Scanner scanner, Token name) {
    ImplElementaryParser parser = new ImplElementaryParser(scanner);
    return parser.parseImplementationElementary(name);
  }

  // EBNF implementationElementary: "elementary" { entryCode | exitCode | compDeclBlock | varDeclBlock | constDeclBlock
  // | privateFunction | entry | exit }
  private ImplElementary parseImplementationElementary(Token name) {
    ElementInfo info = expect(TokenType.ELEMENTARY).getInfo();
    ImplElementary comp = new ImplElementary(name.getInfo(),name.getData());

    Block entryBody = new Block(info);
    Block exitBody = new Block(info);
    FuncEntryExit entryFunc = makeEntryExitFunc(State.ENTRY_FUNC_NAME, entryBody);
    FuncEntryExit exitFunc = makeEntryExitFunc(State.EXIT_FUNC_NAME, exitBody);
    comp.getFunction().add(entryFunc);
    comp.getFunction().add(exitFunc);
    comp.setEntryFunc(new ReferenceLinked(info,entryFunc));
    comp.setExitFunc(new ReferenceLinked(info,exitFunc));

    while (true)
      switch (peek().getType()) {
      case ENTRY: {
        entryBody.getStatements().add(parseEntryCode());
        break;
      }
      case EXIT: {
        exitBody.getStatements().add(parseExitCode());
        break;
      }
      case COMPONENT: {
        comp.getComponent().addAll(parseCompDeclBlock());
        break;
      }
      case VAR: {
        comp.getVariable().addAll(parseVarDefBlock(StateVariable.class));
        break;
      }
      case CONST: {
        comp.getConstant().addAll(parseConstDefBlock(ConstPrivate.class));
        break;
      }
      case FUNCTION: {
        Pair<List<String>, FunctionHeader> func = parsePrivateFunction();
        comp.addFunction(func.first, func.second);
        break;
      }
      default: {
        return comp;
      }
      }
  }

}
