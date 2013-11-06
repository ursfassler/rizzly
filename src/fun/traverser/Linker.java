package fun.traverser;

import java.util.Iterator;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.composition.ImplComposition;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FunctionHeader;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.Transition;
import fun.knowledge.KnowFunChild;
import fun.knowledge.KnowledgeBase;
import fun.other.Component;
import fun.other.ImplElementary;
import fun.other.Named;
import fun.statement.Block;
import fun.statement.VarDefStmt;
import fun.symbol.SymbolTable;
import fun.type.Type;
import fun.variable.TemplateParameter;

//TODO merge with ClassNameExtender?
public class Linker extends RefReplacer<SymbolTable<Named, String>> {
  private KnowledgeBase kb;

  public Linker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  static public void process(Fun obj, KnowledgeBase kb) {
    Linker linker = new Linker(kb);
    SymbolTable<Named, String> sym = new SymbolTable<Named, String>();
    linker.traverse(obj, sym);
  }

  @Override
  protected Reference visitReferenceUnlinked(ReferenceUnlinked obj, SymbolTable<Named, String> param) {
    visitItr(obj.getOffset(), param);

    Iterator<String> itr = obj.getName().iterator();
    assert (itr.hasNext());

    String head = itr.next();
    Named item = param.find(head);
    if (item == null) {
      item = kb.getRoot().find(head);
    }
    if (item == null) {
      RError.err(ErrorType.Error, obj.getInfo(), "Object not found: " + obj.getName());
      return null;
    }

    KnowFunChild kc = kb.getEntry(KnowFunChild.class);
    while (itr.hasNext()) {
      String name = itr.next();
      item = (Named) kc.get(item, name);
      assert (item != null);
    }

    ReferenceLinked linked = new ReferenceLinked(obj.getInfo(), item);
    linked.getOffset().addAll(obj.getOffset());
    return linked;
  }

  @Override
  protected Reference visitReferenceLinked(ReferenceLinked obj, SymbolTable<Named, String> param) {
    visitItr(obj.getOffset(), param);
    return obj;
  }

  @Override
  protected Expression visitComponent(Component obj, SymbolTable<Named, String> param) {
    param = new SymbolTable<Named, String>(param);
    add(param, obj); // add self?
    param.add("Self", obj);
    super.visitComponent(obj, param);
    return null;
  }

  @Override
  protected Expression visitImplComposition(ImplComposition obj, SymbolTable<Named, String> param) {
    super.visitImplComposition(obj, param);
    return null;
  }

  @Override
  protected Expression visitImplElementary(ImplElementary obj, SymbolTable<Named, String> param) {
    /*
     * add( param, obj.getComponent() ); add( param, obj.getConstant() ); add( param, obj.getInternalFunction() ); add(
     * param, obj.getVariable() );
     */
    super.visitImplElementary(obj, param);
    return null;
  }

  @Override
  protected Expression visitImplHfsm(ImplHfsm obj, SymbolTable<Named, String> param) {
    param.add(obj.getTopstate().getName(), obj.getTopstate());
    super.visitImplHfsm(obj, param);
    return null;
  }

  @Override
  protected Expression visitState(State obj, SymbolTable<Named, String> param) {
    param = new SymbolTable<Named, String>(param);
    add(param, obj.getItemList());
    add(param, obj.getVariable());
    super.visitState(obj, param);
    return null;
  }

  @Override
  protected Expression visitStateComposite(StateComposite obj, SymbolTable<Named, String> param) {
    {
      SymbolTable<Named, String> chn = new SymbolTable<Named, String>(param);
      add(chn, obj.getItemList().getItems(State.class));
      obj.setInitial((Reference) visit(obj.getInitial(), chn));
    }
    return super.visitStateComposite(obj, param);
  }

  @Override
  protected Expression visitTransition(Transition obj, SymbolTable<Named, String> param) {
    param = new SymbolTable<Named, String>(param);
    add(param, obj.getParam());
    return super.visitTransition(obj, param);
  }

  @Override
  protected Reference visitFunctionHeader(FunctionHeader obj, SymbolTable<Named, String> param) {
    param = new SymbolTable<Named, String>(param);
    add(param, obj.getParam());
    super.visitFunctionHeader(obj, param);
    return null;
  }

  @Override
  protected Expression visitTemplateParameter(TemplateParameter obj, SymbolTable<Named, String> param) {
    add(param, obj);
    return super.visitTemplateParameter(obj, param);
  }

  @Override
  protected Expression visitType(Type obj, SymbolTable<Named, String> param) {
    return super.visitType(obj, new SymbolTable<Named, String>(param));
  }

  @Override
  protected Reference visitVarDef(VarDefStmt obj, SymbolTable<Named, String> param) {
    add(param, obj.getVariable());
    super.visitVarDef(obj, param);
    return null;
  }

  @Override
  protected Reference visitBlock(Block obj, SymbolTable<Named, String> param) {
    param = new SymbolTable<Named, String>(param);
    super.visitBlock(obj, param);
    return null;
  }

  private void add(SymbolTable<Named, String> sym, Iterable<? extends Named> list) {
    for (Named itr : list) {
      add(sym, itr);
    }
  }

  private void add(SymbolTable<Named, String> sym, Named item) {
    sym.add(item.getName(), item);
  }

}
