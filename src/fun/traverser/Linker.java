package fun.traverser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import common.Designator;
import common.Direction;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.composition.ImplComposition;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.Reference;
import fun.function.impl.FuncEntryExit;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.function.impl.FuncProtRet;
import fun.function.impl.FuncProtVoid;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.Transition;
import fun.other.ImplElementary;
import fun.other.Named;
import fun.other.RizzlyFile;
import fun.statement.Block;
import fun.statement.VarDefStmt;
import fun.symbol.SymbolTable;
import fun.type.Type;
import fun.type.base.TypeAlias;
import fun.type.composed.RecordType;
import fun.type.template.Range;
import fun.type.template.RangeTemplate;

public class Linker extends DefTraverser<Void, SymbolTable> {
  final private HashMap<Designator, RizzlyFile> files = new HashMap<Designator, RizzlyFile>();

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

  private Collection<? extends Named> getPublics(RizzlyFile rzy) {
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

    for (Designator des : obj.getImports()) {
      RizzlyFile rzy = files.get(des);
      assert (rzy != null);
      List<Named> objs = new ArrayList<Named>();
      objs.addAll(getPublics(rzy));
      pubs.addAll(objs);
      rzys.add(rzy);
    }

    locs.addAll(obj.getComp().getList());
    locs.addAll(obj.getConstant().getList());
    locs.addAll(obj.getFunction().getList());
    locs.addAll(obj.getType().getList());
    super.visitRizzlyFile(obj, locs);
    return null;
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
    param.addAll(obj.getIface(Direction.in).getList());
    param.addAll(obj.getIface(Direction.out).getList());

    visitList(obj.getTemplateParam(), param);
    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);

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
    param.addAll(obj.getIface(Direction.in).getList());
    param.addAll(obj.getIface(Direction.out).getList());

    visitList(obj.getTemplateParam(), param);
    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);

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
    param.addAll(obj.getIface(Direction.in).getList());
    param.addAll(obj.getIface(Direction.out).getList());

    visitList(obj.getTemplateParam(), param);
    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);
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
    return super.visitState(obj, param);
  }

  @Override
  protected Void visitTransition(Transition obj, SymbolTable param) {
    // visit(obj.getSrc(), param); // done by TransitionStateLinker
    // visit(obj.getDst(), param);
    visit(obj.getEvent(), param);
    visitList(obj.getParam(), param);

    param = new SymbolTable(param);
    param.addAll(obj.getParam().getList());

    visit(obj.getGuard(), param);    // FIXME do this in the source state space
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
