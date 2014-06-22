package parser;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.function.impl.FuncEntryExit;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.Component;
import fun.statement.Block;
import fun.variable.StateVariable;

public class ImplHfsmParser extends ImplBaseParser {
  public ImplHfsmParser(Scanner scanner) {
    super(scanner);
  }

  public static Component parse(Scanner scanner, Token name) {
    ImplHfsmParser parser = new ImplHfsmParser(scanner);
    return parser.parseImplementationHfsm(name);
  }

  // EBNF implementationComposition: "hfsm" "(" id ")" stateBody
  private Component parseImplementationHfsm(Token name) {
    ElementInfo info = expect(TokenType.HFSM).getInfo();

    expect(TokenType.OPENPAREN);
    String initial = expect(TokenType.IDENTIFIER).getData();
    expect(TokenType.CLOSEPAREN);

    StateComposite topstate = new StateComposite(info, State.TOPSTATE_NAME, initial);
    parseStateBody(topstate);
    return new ImplHfsm(name.getInfo(), name.getData(), topstate);
  }

  // EBNF stateBody: { entryCode | exitCode | varDeclBlock | funcDecl | transitionDecl | state }
  private <T extends State> void parseStateBody(T state) {
    Block entryBody = new Block(state.getInfo());
    Block exitBody = new Block(state.getInfo());
    initEntryExit(state, entryBody, exitBody);

    while (true) {
      switch (peek().getType()) {
      case ENTRY:
        entryBody.getStatements().add(parseEntryCode());
        break;
      case EXIT:
        exitBody.getStatements().add(parseExitCode());
        break;
      case VAR:
        state.getVariable().addAll(parseVarDefBlock(StateVariable.class));
        break;
      case RESPONSE:
      case FUNCTION:
        state.getItemList().add(parsePrivateFunction(peek().getType()));
        break;
      case TRANSITION:
        state.getItemList().addAll(parseTransitionDecl());
        break;
      case STATE:
        if (state instanceof StateComposite) {
          state.getItemList().add(parseState());
        } else {
          RError.err(ErrorType.Error, peek().getInfo(), "Simple state can not have children (no initial state defined)");
        }
        break;
      default:
        return;
      }
    }
  }

  private void initEntryExit(State state, Block entryBody, Block exitBody) {
    FuncEntryExit entryFunc = makeEntryExitFunc(State.ENTRY_FUNC_NAME, entryBody);
    FuncEntryExit exitFunc = makeEntryExitFunc(State.EXIT_FUNC_NAME, exitBody);
    state.getItemList().add(entryFunc);
    state.getItemList().add(exitFunc);
    state.setEntryFuncRef(new Reference(state.getInfo(), entryFunc));
    state.setExitFuncRef(new Reference(state.getInfo(), exitFunc));
  }

  // EBNF stateDecl: "state" id ( ";" | ( [ "(" id ")" ] stateBody "end" ) )
  private State parseState() {
    ElementInfo info = expect(TokenType.STATE).getInfo();
    String name = expect(TokenType.IDENTIFIER).getData();

    if (consumeIfEqual(TokenType.SEMI)) {
      State state = new StateSimple(info, name);
      initEntryExit(state, new Block(state.getInfo()), new Block(state.getInfo()));
      return state;
    } else {
      State state;
      if (consumeIfEqual(TokenType.OPENPAREN)) {
        String initial = expect(TokenType.IDENTIFIER).getData();
        expect(TokenType.CLOSEPAREN);
        state = new StateComposite(info, name, initial);
      } else {
        state = new StateSimple(info, name);
      }

      parseStateBody(state);
      expect(TokenType.END);
      return state;
    }
  }

  // EBNF transitionDecl: "transition" transition { transition }
  private List<Transition> parseTransitionDecl() {
    expect(TokenType.TRANSITION);
    List<Transition> list = new ArrayList<Transition>();
    do {
      list.add(parseTransition());
    } while (peek().getType() == TokenType.IDENTIFIER);
    return list;
  }

  // EBNF transition: nameRef "to" nameRef "by" transitionEvent [ "if" expr ] ( ";" | "do" block "end" )
  private Transition parseTransition() {
    Reference src = parseNameRef();
    ElementInfo info = expect(TokenType.TO).getInfo();
    Reference dst = parseNameRef();

    Transition ret = new Transition(info, "tr" + info.getLine() + "_" + info.getRow());
    ret.setSrc(src);
    ret.setDst(dst);

    expect(TokenType.BY);
    parseTransitionEvent(ret);
    {
      Expression guard;
      if (consumeIfEqual(TokenType.IF)) {
        guard = expr().parse();
      } else {
        guard = new BoolValue(info, true);
      }
      ret.setGuard(guard);
    }
    {
      Block body;
      if (consumeIfEqual(TokenType.SEMI)) {
        body = new Block(info);
      } else {
        expect(TokenType.DO);
        body = stmt().parseBlock();
        expect(TokenType.END);
      }
      ret.setBody(body);
    }
    return ret;
  }

  // EBNF nameRef: Designator
  private Reference parseNameRef() {
    Token tokh = expect(TokenType.IDENTIFIER);
    Reference ret = new Reference(tokh.getInfo(), tokh.getData());

    while (consumeIfEqual(TokenType.PERIOD)) {
      Token tok = expect(TokenType.IDENTIFIER);
      ret.getOffset().add(new RefName(tok.getInfo(), tok.getData()));
    }

    return ret;
  }

  // EBNF transitionEvent: id vardeflist
  private void parseTransitionEvent(Transition ret) {
    Token tok = expect(TokenType.IDENTIFIER);
    Reference name = new Reference(tok.getInfo(), tok.getData());
    ret.setEvent(name);
    ret.getParam().addAll(parseVardefList());
  }

}
