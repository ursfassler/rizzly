package fun.traverser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Designator;
import common.Direction;

import error.ErrorType;
import error.RError;
import fun.DefGTraverser;
import fun.Fun;
import fun.NullTraverser;
import fun.composition.ImplComposition;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FunctionHeader;
import fun.generator.ComponentGenerator;
import fun.generator.Generator;
import fun.generator.InterfaceGenerator;
import fun.generator.TypeGenerator;
import fun.hfsm.FullStateName;
import fun.hfsm.ImplHfsm;
import fun.hfsm.QueryItem;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.Component;
import fun.other.ImplElementary;
import fun.other.Interface;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.other.RizzlyFile;
import fun.symbol.NameTable;
import fun.symbol.SymbolTable;
import fun.type.Type;
import fun.type.base.EnumType;
import fun.type.base.TypeAlias;
import fun.type.composed.RecordType;
import fun.type.composed.UnionType;
import fun.type.template.UserTypeGenerator;
import fun.variable.CompUse;
import fun.variable.Constant;
import fun.variable.FuncVariable;
import fun.variable.IfaceUse;
import fun.variable.StateVariable;
import fun.variable.TemplateParameter;

//TODO what if interface and component have the same name? (in different namespaces)
//TODO what if component is specified with namespace prefix?

/**
 * Extends references to types and other class stuff to the full name path
 * 
 * @author urs
 * 
 */
public class ClassNameExtender extends DefGTraverser<Void, SymbolTable<Designator, String>> {
  private Map<Designator, RizzlyFile> rfile;
  private HashMap<State, Designator> fullName = null;
  private HashMap<Designator, SymbolTable<Designator, String>> stateTable = null;

  public ClassNameExtender(Collection<RizzlyFile> files) {
    super();
    rfile = new HashMap<Designator, RizzlyFile>();
    for (RizzlyFile file : files) {
      rfile.put(file.getName(), file);
    }
  }

  public static void process(Collection<RizzlyFile> files, SymbolTable<Designator, String> sym) {
    sym = new SymbolTable<Designator, String>(sym);
    Set<String> ns = new HashSet<String>();
    for (RizzlyFile file : files) {
      ArrayList<String> name = file.getName().toList();
      ns.add(name.get(0));
    }
    for (String name : ns) {
      sym.add(name, new Designator(name));
    }
    ClassNameExtender extender = new ClassNameExtender(files);
    for (RizzlyFile file : files) {
      extender.traverse(file, sym);
    }
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, SymbolTable<Designator, String> param) {
    param = new SymbolTable<Designator, String>(param);
    List<Designator> imported = makeImportList(obj.getImports());
    addImports(imported, param);
    param = new SymbolTable<Designator, String>(param);

    AdderWithPrefix.add(obj.getCompfunc(), obj.getName(), param);
    AdderWithPrefix.add(obj.getType(), obj.getName(), param);
    AdderWithPrefix.add(obj.getIface(), obj.getName(), param);
    AdderWithPrefix.add(obj.getComp(), obj.getName(), param);
    AdderWithPrefix.add(obj.getConstant(), obj.getName(), param);
    AdderWithPrefix.add(obj.getFunction(), obj.getName(), param);

    super.visitRizzlyFile(obj, param);
    return null;
  }

  private List<Designator> makeImportList(List<Designator> imports) {
    List<Designator> ret = new ArrayList<Designator>();

    for (Designator imp : imports) {
      RizzlyFile file = rfile.get(imp);
      assert (file != null);
      List<String> objNames = makeObjList(file);
      for (String name : objNames) {
        Designator des = new Designator(imp, name);
        ret.add(des);
      }
    }

    return ret;
  }

  private List<String> makeObjList(RizzlyFile file) {
    List<String> list = (new Getter<String, Void>() {
      @Override
      protected Void visit(Fun obj, Void param) {
        if (obj instanceof Named) {
          return add(((Named) obj).getName());
        } else {
          return super.visit(obj, param);
        }
      }
    }).get(file, null);
    return list;
  }

  private void addImports(List<Designator> list, SymbolTable<Designator, String> param) {
    NameTable table = new NameTable();
    for (Designator des : list) {
      table.addName(des);
      List<String> name = des.toList();
      table.addShort(name.get(name.size() - 1), des);
    }
    for (String name : table.getAlias().keySet()) {
      param.add(name, table.expand(name));
    }
  }

  @Override
  protected Void visitInterface(Interface obj, SymbolTable<Designator, String> param) {
    param = new SymbolTable<Designator, String>(param);
    addNames(obj.getPrototype(), param);
    super.visitInterface(obj, param);
    return null;
  }

  @Override
  protected Void visitComponent(Component obj, SymbolTable<Designator, String> param) {
    param = new SymbolTable<Designator, String>(param);
    AdderWithPrefix.add(obj.getIface(Direction.in), new Designator("Self"), param);

    AdderWithPrefix.add(obj.getIface(Direction.out), new Designator("Self"), param);
    // we need that, otherwise the namespace linker may link to the wrong namespace
    super.visitComponent(obj, param);

    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, SymbolTable<Designator, String> param) {
    AdderWithPrefix.add(obj.getConstant(), new Designator("Self"), param);
    AdderWithPrefix.add(obj.getVariable(), new Designator("Self"), param);

    AdderWithPrefix.add(obj.getComponent(), new Designator("Self"), param);

    ListOfNamed<FunctionHeader> cofu = new ListOfNamed<FunctionHeader>(obj.getFunction().getItems(FunctionHeader.class));
    AdderWithPrefix.add(cofu, new Designator("Self"), param);

    super.visitImplElementary(obj, param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, SymbolTable<Designator, String> param) {
    AdderWithPrefix.add(obj.getComponent(), new Designator("Self"), param);
    super.visitImplComposition(obj, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, SymbolTable<Designator, String> param) {
    assert (fullName == null);
    assert (stateTable == null);
    stateTable = new HashMap<Designator, SymbolTable<Designator, String>>();
    fullName = FullStateName.get(obj.getTopstate());
    super.visitImplHfsm(obj, param);
    stateTable = null;
    fullName = null;
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, SymbolTable<Designator, String> param) {
    Designator key = fullName.get(obj);
    param = new SymbolTable<Designator, String>(param);

    param.add(obj.getName(), fullName.get(obj));
    AdderWithPrefix.add(obj.getBfunc(), key, param);
    AdderWithPrefix.add(obj.getVariable(), key, param);

    stateTable.put(key, param);

    visit(obj.getEntryFuncRef(), param);
    visit(obj.getExitFuncRef(), param);
    visitList(obj.getBfunc(), param);
    visitList(obj.getVariable(), param);

    // param.add(obj.getName(), new Designator(obj.getName()));
    visitItr(obj.getItem(), param);

    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, SymbolTable<Designator, String> param) {
    Designator key = fullName.get(obj);
    ListOfNamed<State> children = new ListOfNamed<State>(obj.getItemList(State.class));
    {
      SymbolTable<Designator, String> initsym = new SymbolTable<Designator, String>();
      AdderWithPrefix.add(children, key, initsym);
      visit(obj.getInitial(), initsym);
    }

    param = new SymbolTable<Designator, String>(param);
    param.add(obj.getName(), fullName.get(obj));
    AdderWithPrefix.add(obj.getBfunc(), key, param);
    AdderWithPrefix.add(obj.getVariable(), key, param);

    stateTable.put(key, param);

    visit(obj.getEntryFuncRef(), param);
    visit(obj.getExitFuncRef(), param);
    visitList(obj.getBfunc(), param);
    visitList(obj.getVariable(), param);
    visitItr(children, param);
    visitItr(obj.getItemList(QueryItem.class), param);

    param = new SymbolTable<Designator, String>(param);
    // param.add(obj.getName(), new Designator(obj.getName()));

    NameTable table = NameTableCreator.make(children, fullName.get(obj));
    for (String name : table.getAlias().keySet()) {
      param.add(name, table.expand(name));
    }

    visitItr(obj.getItemList(Transition.class), param);

    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, SymbolTable<Designator, String> param) {
    param = new SymbolTable<Designator, String>(param);

    visit(obj.getSrc(), param);
    visit(obj.getDst(), param);
    visit(obj.getEvent(), param);
    visitItr(obj.getParam(), param);
    visit(obj.getBody(), param);

    Designator key = ((ReferenceUnlinked) obj.getSrc()).getName();
    SymbolTable<Designator, String> scopeSym = stateTable.get(key);
    scopeSym = new SymbolTable<Designator, String>(scopeSym);
    addNames(obj.getParam(), scopeSym);

    visit(obj.getGuard(), scopeSym);

    return null;
  }

  private void addNames(ListOfNamed<? extends Named> list, SymbolTable<Designator, String> param) {
    for (Named name : list) {
      Designator old = param.find(name.getName(), false);
      if (old != null) {
        RError.err(ErrorType.Error, name.getInfo(), "Name already defined: " + name.getName());
      }
      param.add(name.getName(), new Designator(name.getName()));
    }
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, SymbolTable<Designator, String> param) {
    super.visitQueryItem(obj, param);
    return null;
  }

  @Override
  protected Void visitReferenceUnlinked(ReferenceUnlinked obj, SymbolTable<Designator, String> param) {
    visitItr(obj.getOffset(), param);

    if (!obj.getName().toList().isEmpty()) {
      return null;
    }

    assert (!obj.getOffset().isEmpty());
    assert (obj.getOffset().get(0) instanceof RefName);
    String name = ((RefName) obj.getOffset().get(0)).getName();

    // TODO tell user if name is not available because it is ambiguous
    Designator pre = param.get(name, obj.getInfo());
    assert (pre != null);

    obj.setName(pre);
    obj.getOffset().pop();

    return null;
  }

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, SymbolTable<Designator, String> param) {
    return super.visitReferenceLinked(obj, param);
  }

  @Override
  protected Void visitRefCompcall(RefTemplCall obj, SymbolTable<Designator, String> param) {
    visitItr(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, SymbolTable<Designator, String> param) {
    return null;
  }

  @Override
  protected Void visitRefCall(RefCall obj, SymbolTable<Designator, String> param) {
    visitItr(obj.getActualParameter(), param);
    return null;
  }

  @Override
  protected Void visitRefIndex(RefIndex obj, SymbolTable<Designator, String> param) {
    visit(obj.getIndex(), param);
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, SymbolTable<Designator, String> param) {
    param.add(obj.getName(), new Designator(obj.getName()));
    return super.visitFuncVariable(obj, param);
  }

  @Override
  protected Void visitInterfaceGenerator(InterfaceGenerator obj, SymbolTable<Designator, String> param) {
    return super.visitInterfaceGenerator(obj, generator(obj, param));
  }

  @Override
  protected Void visitComponentGenerator(ComponentGenerator obj, SymbolTable<Designator, String> param) {
    return super.visitComponentGenerator(obj, generator(obj, param));
  }

  @Override
  protected Void visitUserTypeGenerator(UserTypeGenerator obj, SymbolTable<Designator, String> param) {
    return super.visitUserTypeGenerator(obj, generator(obj, param));
  }

  private SymbolTable<Designator, String> generator(Generator obj, SymbolTable<Designator, String> param) {
    param = new SymbolTable<Designator, String>(param);
    for (TemplateParameter gen : obj.getParam()) {
      param.add(gen.getName(), new Designator(gen.getName()));
    }
    param.add("Self", new Designator(obj.getName()));
    return param;
  }

  @Override
  protected Void visitType(Type obj, SymbolTable<Designator, String> param) {
    param = new SymbolTable<Designator, String>(param);
    return super.visitType(obj, param);
  }

  @Override
  protected Void visitFunctionHeader(FunctionHeader obj, SymbolTable<Designator, String> param) {
    param = new SymbolTable<Designator, String>(param);
    return super.visitFunctionHeader(obj, param);
  }

}

class NameTableCreator extends NullTraverser<Void, Designator> {
  final private NameTable table = new NameTable();

  static public NameTable make(Iterable<? extends Fun> list, Designator prefix) {
    NameTableCreator creator = new NameTableCreator();
    creator.visitItr(list, prefix);
    return creator.getTable();
  }

  public NameTable getTable() {
    return table;
  }

  @Override
  protected Void visitDefault(Fun obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitTransition(Transition obj, Designator param) {
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.getName());
    table.addName(param);
    table.addShort(obj.getName(), param);
    return super.visitState(obj, param);
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, Designator param) {
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, Designator param) {
    visitItr(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, Designator param) {
    return null; // not referencable
  }

}

class AdderWithPrefix extends NullTraverser<Void, Designator> {
  private SymbolTable<Designator, String> sym;

  public AdderWithPrefix(SymbolTable<Designator, String> sym) {
    super();
    this.sym = sym;
  }

  static public void add(ListOfNamed<? extends Named> list, Designator prefix, SymbolTable<Designator, String> sym) {
    AdderWithPrefix fixer = new AdderWithPrefix(sym);
    fixer.visitItr(list, prefix);
  }

  private void add(Named obj, Designator param) {
    sym.add(obj.getName(), new Designator(param, obj.getName()));
  }

  @Override
  protected Void visitDefault(Fun obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitFunctionHeader(FunctionHeader obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitIfaceUse(IfaceUse obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, Designator param) {
    add(obj, param);
    visitItr(obj.getElement(), new Designator(param, obj.getName()));
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitInterface(Interface obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitComponent(Component obj, Designator param) {
    add(obj, param);
    return null;
  }

  @Override
  protected Void visitTypeGenerator(TypeGenerator obj, Designator param) {
    add(obj, param); // FIXME ok?
    return null;
  }

  @Override
  protected Void visitInterfaceGenerator(InterfaceGenerator obj, Designator param) {
    add(obj, param); // FIXME ok?
    return null;
  }

  @Override
  protected Void visitComponentGenerator(ComponentGenerator obj, Designator param) {
    add(obj, param); // FIXME ok?
    return null;
  }

}
