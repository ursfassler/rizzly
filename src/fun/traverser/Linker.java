package fun.traverser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import common.Designator;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.composition.ImplComposition;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.Reference;
import fun.function.impl.FuncEntryExit;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncImplResponse;
import fun.function.impl.FuncImplSlot;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.function.impl.FuncProtRet;
import fun.function.impl.FuncProtVoid;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.ImplElementary;
import fun.other.Named;
import fun.other.RizzlyFile;
import fun.other.SymbolTable;
import fun.statement.Block;
import fun.statement.VarDefStmt;
import fun.type.Type;
import fun.type.base.TypeAlias;
import fun.type.composed.RecordType;
import fun.type.template.Range;
import fun.type.template.RangeTemplate;

public class Linker extends DefTraverser<Void, SymbolTable> {
  final private HashMap<Designator, RizzlyFile> files = new HashMap<Designator, RizzlyFile>();
  final private HashMap<State, SymbolTable> stateNames = new HashMap<State, SymbolTable>();

  public Linker(Collection<RizzlyFile> fileList) {
    for (RizzlyFile file : fileList) {
      files.put(file.getFullName(), file);
    }
  }

  public static void process(Collection<? extends Fun> itms, Collection<RizzlyFile> fileList, SymbolTable sym) {
    Linker linker = new Linker(fileList);
    for (Fun itm : itms) {
      linker.traverse(itm, sym);
    }
  }

  public static void process(Collection<RizzlyFile> fileList, SymbolTable sym) {
    Linker linker = new Linker(fileList);
    for (Fun file : fileList) {
      linker.traverse(file, sym);
    }
  }

  static public Collection<? extends Named> getPublics(RizzlyFile rzy) {
    Collection<Named> ret = new ArrayList<Named>();
    ret.addAll(rzy.getComp().getList());
    ret.addAll(rzy.getConstant().getList());
    ret.addAll(rzy.getFunction().getList());
    ret.addAll(rzy.getType().getList());
    return ret;
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, SymbolTable param) {
    SymbolTable pubs = new SymbolTable(param);
    SymbolTable rzys = new SymbolTable(pubs);
    SymbolTable locs = new SymbolTable(rzys);

    List<Named> objs = new ArrayList<Named>();
    for (Designator des : obj.getImports()) {
      RizzlyFile rzy = files.get(des);
      assert (rzy != null);
      objs.addAll(getPublics(rzy));
      rzys.add(rzy);
    }

    pubs.addAll(removeDuplicates(objs));

    locs.addAll(obj.getComp().getList());
    locs.addAll(obj.getConstant().getList());
    locs.addAll(obj.getFunction().getList());
    locs.addAll(obj.getType().getList());
    super.visitRizzlyFile(obj, locs);
    return null;
  }

  private Collection<Named> removeDuplicates(List<Named> objs) {
    Set<String> ambigous = new HashSet<String>();
    HashMap<String, Named> map = new HashMap<String, Named>();
    for (Named itr : objs) {
      if (!ambigous.contains(itr.getName())) {
        if (map.containsKey(itr.getName())) {
          map.remove(itr.getName());
          ambigous.add(itr.getName());
        } else {
          map.put(itr.getName(), itr);
        }
      }
    }
    return map.values();
  }

  @Override
  protected Void visitReference(Reference obj, SymbolTable param) {
    if (obj.getLink() instanceof DummyLinkTarget) {
      String name = obj.getLink().getName();

      Named link = param.find(name);
      if (link == null) {
        RError.err(ErrorType.Error, obj.getInfo(), "Name not found: " + name);
      }
      assert (!(link instanceof DummyLinkTarget));

      obj.setLink(link);
    }
    return super.visitReference(obj, param);
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getTemplateParam().getList());
    param.addAll(obj.getQuery().getList());
    param.addAll(obj.getSignal().getList());
    param.addAll(obj.getResponse().getList());
    param.addAll(obj.getSlot().getList());

    visitList(obj.getTemplateParam(), param);
    visitList(obj.getQuery(), param);
    visitList(obj.getSignal(), param);
    visitList(obj.getResponse(), param);
    visitList(obj.getSlot(), param);

    // TODO separate component header and implementation?
    param = new SymbolTable(param);
    param.addAll(obj.getComponent().getList());

    visitList(obj.getComponent(), param);
    visitItr(obj.getConnection(), param);

    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getTemplateParam().getList());
    param.addAll(obj.getQuery().getList());
    param.addAll(obj.getSignal().getList());
    param.addAll(obj.getResponse().getList());
    param.addAll(obj.getSlot().getList());

    visitList(obj.getTemplateParam(), param);
    visitList(obj.getQuery(), param);
    visitList(obj.getSignal(), param);
    visitList(obj.getResponse(), param);
    visitList(obj.getSlot(), param);

    // TODO separate component header and implementation?
    param = new SymbolTable(param);
    param.addAll(obj.getConstant().getList());
    param.addAll(obj.getVariable().getList());
    param.addAll(obj.getFunction().getList());

    visitList(obj.getConstant(), param);
    visitList(obj.getVariable(), param);
    visitList(obj.getFunction(), param);
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);

    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getTemplateParam().getList());
    param.addAll(obj.getQuery().getList());
    param.addAll(obj.getSignal().getList());
    param.addAll(obj.getResponse().getList());
    param.addAll(obj.getSlot().getList());

    visitList(obj.getTemplateParam(), param);
    visitList(obj.getQuery(), param);
    visitList(obj.getSignal(), param);
    visitList(obj.getResponse(), param);
    visitList(obj.getSlot(), param);
    // TODO separate component header and implementation?

    TransitionStateLinker.process(obj);

    param = new SymbolTable(param);
    visit(obj.getTopstate(), param);

    return null;
  }

  @Override
  protected Void visitState(State obj, SymbolTable param) {
    param = new SymbolTable(param);

    param.addAll(obj.getVariable().getList());
    param.addAll(obj.getItemList().getList());

    assert (!stateNames.containsKey(obj));
    stateNames.put(obj, param);

    return super.visitState(obj, param);
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, SymbolTable param) {
    visitItr(obj.getVariable(), param);
    visit(obj.getEntryFuncRef(), param);
    visit(obj.getExitFuncRef(), param);

    List<Transition> trans = obj.getItemList().getItems(Transition.class);
    List<Named> rest = new LinkedList<Named>(obj.getItemList().getList());
    rest.removeAll(trans);
    visitItr(rest, param);
    visitItr(trans, param);
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, SymbolTable param) {
    visitItr(obj.getVariable(), param);
    visit(obj.getInitial(), param);
    visit(obj.getEntryFuncRef(), param);
    visit(obj.getExitFuncRef(), param);

    List<Transition> trans = obj.getItemList().getItems(Transition.class);
    List<Named> rest = new LinkedList<Named>(obj.getItemList().getList());
    rest.removeAll(trans);
    visitItr(rest, param);
    visitItr(trans, param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, SymbolTable param) {
    // visit(obj.getSrc(), param); // done by TransitionStateLinker
    // visit(obj.getDst(), param);
    visit(obj.getEvent(), param);
    visitList(obj.getParam(), param);

    param = new SymbolTable(param);
    param.addAll(obj.getParam().getList());

    // get context from src state and add event arguments
    SymbolTable srcNames = stateNames.get(obj.getSrc().getLink());
    assert (srcNames != null);
    srcNames = new SymbolTable(srcNames);
    srcNames.addAll(obj.getParam().getList());
    visit(obj.getGuard(), srcNames);

    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getParam().getList());
    super.visitFuncPrivateRet(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getParam().getList());
    super.visitFuncPrivateVoid(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncImplResponse(FuncImplResponse obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getParam().getList());
    super.visitFuncImplResponse(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncImplSlot(FuncImplSlot obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getParam().getList());
    super.visitFuncImplSlot(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncProtRet(FuncProtRet obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getParam().getList());
    super.visitFuncProtRet(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncProtVoid(FuncProtVoid obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getParam().getList());
    super.visitFuncProtVoid(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncGlobal(FuncGlobal obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getTemplateParam().getList());
    param.addAll(obj.getParam().getList());
    super.visitFuncGlobal(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncPrivate(FuncPrivateVoid obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getParam().getList());
    super.visitFuncPrivate(obj, param);
    return null;
  }

  @Override
  protected Void visitFuncEntryExit(FuncEntryExit obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getParam().getList());
    super.visitFuncEntryExit(obj, param);
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getTemplateParam().getList());
    super.visitRecordType(obj, param);
    return null;
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getTemplateParam().getList());
    super.visitTypeAlias(obj, param);
    return null;
  }

  @Override
  protected Void visitRange(Range obj, SymbolTable param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitRangeTemplate(RangeTemplate obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getTemplateParam().getList());
    super.visitRangeTemplate(obj, param);
    return null;
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
