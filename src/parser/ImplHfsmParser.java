package parser;

import java.util.ArrayList;
import java.util.List;

import util.Pair;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FunctionHeader;
import fun.function.impl.FuncEntryExit;
import fun.function.impl.FuncPrivateRet;
import fun.hfsm.ImplHfsm;
import fun.hfsm.QueryItem;
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

  public static Component parse(Scanner scanner) {
    ImplHfsmParser parser = new ImplHfsmParser(scanner);
    return parser.parseImplementationHfsm();
  }

  // EBNF implementationComposition: "hfsm" "(" id ")" stateBody
  private Component parseImplementationHfsm() {
    ElementInfo info = expect(TokenType.HFSM).getInfo();

    expect(TokenType.OPENPAREN);
    String initial = expect(TokenType.IDENTIFIER).getData();
    expect(TokenType.CLOSEPAREN);

    StateComposite topstate = new StateComposite(info, State.TOPSTATE_NAME, initial);
    parseStateBody(topstate);
    return new ImplHfsm(info, topstate);
  }

  // EBNF stateBody: { entryCode | exitCode | varDeclBlock | funcDecl | transitionDecl | state }
  private <T extends State> void parseStateBody(T state) {
    Block entryBody = new Block(state.getInfo());
    Block exitBody = new Block(state.getInfo());
    FuncEntryExit entryFunc = makeEntryExitFunc(State.ENTRY_FUNC_NAME, entryBody);
    FuncEntryExit exitFunc = makeEntryExitFunc(State.EXIT_FUNC_NAME, exitBody);
    state.getBfunc().add(entryFunc);
    state.getBfunc().add(exitFunc);
    state.setEntryFuncRef(new ReferenceLinked(state.getInfo(), entryFunc));
    state.setExitFuncRef(new ReferenceLinked(state.getInfo(), exitFunc));

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
      case FUNCTION:
        Pair<List<String>, FunctionHeader> func = parsePrivateFunction();
        if (func.first.isEmpty()) {
          state.getBfunc().add(func.second);
        } else {
          assert (func.first.size() == 1);
          state.getItem().add(new QueryItem(func.first.get(0), (FuncPrivateRet) func.second));
        }
        break;
      case TRANSITION:
        state.getItem().addAll(parseTransitionDecl());
        break;
      case STATE:
        if (state instanceof StateComposite) {
          state.getItem().add(parseState());
        } else {
          RError.err(ErrorType.Error, peek().getInfo(), "Simple state can not have children (no initial state defined)");
        }
        break;
      default:
        return;
      }
    }
  }

  // EBNF stateDecl: "state" id [ "(" id ")" ] stateBody "end"
  private State parseState() {
    ElementInfo info = expect(TokenType.STATE).getInfo();
    String name = expect(TokenType.IDENTIFIER).getData();
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

    Transition ret = new Transition(info);
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
    Reference ret = new ReferenceUnlinked(peek().getInfo());

    do {
      Token tok = expect(TokenType.IDENTIFIER);
      ret.getOffset().add(new RefName(tok.getInfo(), tok.getData()));
    } while (consumeIfEqual(TokenType.PERIOD));

    return ret;
  }

  // EBNF transitionEvent: nameRef vardeflist
  private void parseTransitionEvent(Transition ret) {
    Reference name = parseNameRef();
    ret.setEvent(name);
    ret.getParam().addAll(parseVardefList());
  }

}
