package fun.traverser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import common.Designator;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.composition.ImplComposition;
import fun.expression.reference.BaseRef;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefName;
import fun.function.FuncHeader;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateContent;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.CompImpl;
import fun.other.FunList;
import fun.other.ImplElementary;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.other.SymbolTable;
import fun.other.Template;
import fun.statement.Block;
import fun.statement.VarDefStmt;
import fun.type.Type;
import fun.type.composed.RecordType;
import fun.type.template.Range;

public class Linker extends DefTraverser<Void, SymbolTable> {
  final private Namespace files;
  final private HashMap<State, SymbolTable> stateNames = new HashMap<State, SymbolTable>();

  public Linker(Namespace fileList) {
    files = fileList;
  }

  public static void process(Fun fun, Namespace fileList, SymbolTable sym) {
    Linker linker = new Linker(fileList);
    linker.traverse(fun, sym);
  }

  public static void process(FunList<? extends Fun> fun, Namespace fileList, SymbolTable sym) {
    Linker linker = new Linker(fileList);
    for (Fun itr : fun) {
      linker.traverse(itr, sym);
    }
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, SymbolTable param) {
    SymbolTable pubs = new SymbolTable(param);
    SymbolTable rzys = new SymbolTable(pubs);
    SymbolTable locs = new SymbolTable(rzys);

    FunList<Named> objs = new FunList<Named>();
    for (Designator des : obj.getImports()) {
      RizzlyFile rzy = (RizzlyFile) files.getChildItem(des.toList());
      assert (rzy != null);
      FunList<Named> named = rzy.getObjects().getItems(Named.class);
      objs.addAll(named);
      objs.add(rzy);
    }

    pubs.addAll(removeDuplicates(objs));

    locs.addAll(obj.getObjects());

    super.visitRizzlyFile(obj, locs);
    return null;
  }

  private FunList<Named> removeDuplicates(FunList<Named> objs) {
    Set<String> ambigous = new HashSet<String>();
    FunList<Named> map = new FunList<Named>();
    for (Named itr : objs) {
      if (!ambigous.contains(itr.getName())) {
        if (map.find(itr.getName()) != null) {
          map.remove(itr.getName());
          ambigous.add(itr.getName());
        } else {
          map.add(itr);
        }
      }
    }
    return map;
  }

  @Override
  protected Void visitDeclaration(Template obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getTempl());
    visitList(obj.getTempl(), param);
    visit(obj.getObject(), param);
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, SymbolTable param) {
    // TODO: this needs special linking and may not be possible from beginning (but after evaluation)
    return null;
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, SymbolTable param) {
    if (obj.getLink() instanceof DummyLinkTarget) {
      String name = ((DummyLinkTarget) obj.getLink()).getName();

      Named link = param.find(name);
      if (link == null) {
        RError.err(ErrorType.Error, obj.getInfo(), "Name not found: " + name);
      }
      assert (!(link instanceof DummyLinkTarget));

      obj.setLink(link);
    }
    return super.visitBaseRef(obj, param);
  }

  @Override
  protected Void visitComponent(CompImpl obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getObjects());
    super.visitComponent(obj, param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getConnection());
    param.addAll(obj.getInstantiation());
    super.visitImplComposition(obj, param);
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getDeclaration());
    param.addAll(obj.getInstantiation());
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    super.visitImplElementary(obj, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, SymbolTable param) {
    param = new SymbolTable(param);
    TransitionStateLinker.process(obj);

    param = new SymbolTable(param);
    visitList(obj.getInterface(), param);
    param.addAll(obj.getInterface());
    visit(obj.getTopstate(), param);

    return null;
  }

  @Override
  protected Void visitState(State obj, SymbolTable param) {
    param = new SymbolTable(param);

    param.addAll(obj.getItemList());

    assert (!stateNames.containsKey(obj));
    stateNames.put(obj, param);

    // visitList(obj.getItemList(), param);
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);

    super.visitState(obj, param);

    FunList<Transition> trans = obj.getItemList().getItems(Transition.class);
    FunList<StateContent> rest = new FunList<StateContent>(obj.getItemList());
    rest.removeAll(trans);
    visitList(rest, param);
    visitList(trans, param);

    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, SymbolTable param) {
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, SymbolTable param) {
    visit(obj.getInitial(), param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, SymbolTable param) {
    // visit(obj.getSrc(), param); // done by TransitionStateLinker
    // visit(obj.getDst(), param);
    visit(obj.getEvent(), param);
    visitList(obj.getParam(), param);

    param = new SymbolTable(param);
    param.addAll(obj.getParam());

    // get context from src state and add event arguments
    SymbolTable srcNames = stateNames.get(obj.getSrc().getLink());
    assert (srcNames != null);
    srcNames = new SymbolTable(srcNames);
    srcNames.addAll(obj.getParam());
    visit(obj.getGuard(), srcNames);

    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitFunctionHeader(FuncHeader obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getParam());
    super.visitFunctionHeader(obj, param);
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, SymbolTable param) {
    param = new SymbolTable(param);
    super.visitRecordType(obj, param);
    return null;
  }

  @Override
  protected Void visitRange(Range obj, SymbolTable param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitType(Type obj, SymbolTable param) {
    return super.visitType(obj, new SymbolTable(param));
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, SymbolTable param) {
    param.add(obj.getVariable());
    super.visitVarDef(obj, param);
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, SymbolTable param) {
    param = new SymbolTable(param);
    super.visitBlock(obj, param);
    return null;
  }

}
