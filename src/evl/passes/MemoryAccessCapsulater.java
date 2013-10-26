package evl.passes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.ssa.PhiInserter;

import common.Designator;
import common.ElementInfo;
import common.NameFactory;

import evl.DefTraverser;
import evl.cfg.BasicBlock;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.reference.RefPtrDeref;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.statement.bbend.BasicBlockEnd;
import evl.statement.normal.Assignment;
import evl.statement.normal.GetElementPtr;
import evl.statement.normal.LoadStmt;
import evl.statement.normal.NormalStmt;
import evl.statement.normal.StackMemoryAlloc;
import evl.statement.normal.StoreStmt;
import evl.statement.normal.VarDefStmt;
import evl.statement.phi.PhiStmt;
import evl.traverser.ClassGetter;
import evl.traverser.StatementReplacer;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.special.PointerType;
import evl.variable.Constant;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;

/**
 * Inserts a load statement for every read access on memory and a store statement for every write access.
 * @author urs
 */
public class MemoryAccessCapsulater {

  public static void process(Namespace aclasses, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);
    Map<Variable, Variable> map = new HashMap<Variable, Variable>();

    List<StateVariable> svl = ClassGetter.get(StateVariable.class, aclasses);
    for( StateVariable obj : svl ) {
      Type type = obj.getType().getRef();
      assert ( !( type instanceof PointerType ) );
      type = kbi.getPointerType(type);
      obj.setType(new TypeRef(new ElementInfo(), type));

      map.put(obj, obj);
    }


    StackMemTrav smt = new StackMemTrav(kb);
    smt.traverse(aclasses, null);
    map.putAll(smt.getMap());


    DerefInserterAndRelinker diar = new DerefInserterAndRelinker();
    diar.traverse(aclasses, map);
    StmtReplacer loadInserter = new StmtReplacer(kb);
    loadInserter.traverse(aclasses, null);
  }
}



class StmtReplacer extends StatementReplacer<List<NormalStmt>> {

  private KnowledgeBase kb;
  ExprReplacer exprReplacer;

  public StmtReplacer(KnowledgeBase kb) {
    this.kb = kb;
    exprReplacer = new ExprReplacer(kb);
  }
  
  @Override
  protected List<NormalStmt> visitNormalStmt(NormalStmt obj, List<NormalStmt> param) {
    assert ( param == null );
    param = new ArrayList<NormalStmt>();
    List<NormalStmt> ret = super.visitNormalStmt(obj, param);
    assert ( ret == null );
    if( !( obj instanceof Assignment ) ) {  // hacky
      param.add(obj);
    }
    return param;
  }

  @Override
  protected List<NormalStmt> visitBasicBlockEnd(BasicBlockEnd obj, List<NormalStmt> param) {
    assert ( param == null );
    param = new ArrayList<NormalStmt>();
    List<NormalStmt> ret = super.visitBasicBlockEnd(obj, param);
    assert ( ret == null );
    return param;
  }
  
  @Override
  protected List<NormalStmt> visitPhi(PhiStmt phi, BasicBlock in, List<NormalStmt> param) {
    assert ( param == null );
    param = new ArrayList<NormalStmt>();
    Expression expr = phi.getArg(in);
    assert(expr != null);
    List<NormalStmt> ret = visit(expr,param);
    assert ( ret == null );
    return param;
  }

  @Override
  protected List<NormalStmt> visitGetElementPtr(GetElementPtr obj, List<NormalStmt> param) {
    assert ( param != null );
    return null;
  }

  @Override
  protected List<NormalStmt> visitExpression(Expression obj, List<NormalStmt> param) {
    assert ( param != null );
    exprReplacer.traverse(obj, param);
    return null;
  }

  @Override
  protected List<NormalStmt> visitAssignment(Assignment obj, List<NormalStmt> param) {
    assert ( param != null );
    visit(obj.getRight(), param);
    visitItr(obj.getLeft().getOffset(), param);

    if( ExprReplacer.isMemoryAccess(obj.getLeft()) ) {
      GetElementPtr ptr = ExprReplacer.makeGep(obj.getLeft(), kb);

      StoreStmt store = new StoreStmt(obj.getInfo(), new Reference(obj.getInfo(), ptr.getVariable()), obj.getRight());
      param.add(ptr);
      param.add(store);
    }
    return null;
  }

  @Override
  protected List<NormalStmt> visitConstant(Constant obj, List<NormalStmt> param) {
    // do not handle stuff that is not possible to handle
    assert( param == null );
    return null;
  }

}

class ExprReplacer extends DefTraverser<Void, List<NormalStmt>> {

  private KnowledgeBase kb;

  public ExprReplacer(KnowledgeBase kb) {
    this.kb = kb;
  }

  static boolean isMemoryAccess(Reference ref) {
    boolean ret = !ref.getOffset().isEmpty() && ( ref.getOffset().get(0) instanceof RefPtrDeref );
    if( ret ) {
      assert ( ref.getLink() instanceof Variable );
      Variable var = (Variable) ref.getLink();
      assert ( ( var instanceof StateVariable ) || !PhiInserter.isScalar(var.getType().getRef()) );
    }
    return ret;
  }

  @Override
  protected Void visitGetElementPtr(GetElementPtr obj, List<NormalStmt> param) {
    return null;
  }

  static GetElementPtr makeGep(Reference ref, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    Type bt = ( (Variable) ref.getLink() ).getType().getRef();
    assert ( bt instanceof PointerType );

    Type type = ExpressionTypeChecker.process(ref, kb);

    PointerType pt = kbi.getPointerType(type);
    SsaVariable addr = new SsaVariable(ref.getInfo(), NameFactory.getNew(), new TypeRef(ref.getInfo(), pt));

    ref = Copy.copy(ref);
    GetElementPtr ptr = new GetElementPtr(ref.getInfo(), addr, ref);
    return ptr;
  }

  @Override
  protected Void visitReference(Reference obj, List<NormalStmt> param) {
    if( isMemoryAccess(obj) ) {
      assert ( param != null );
      GetElementPtr ptr = makeGep(obj, kb);
      Type type = ptr.getVariable().getType().getRef();
      assert ( type instanceof PointerType );
      type = ( (PointerType) type ).getType().getRef();
      SsaVariable var = new SsaVariable(obj.getInfo(), NameFactory.getNew(), new TypeRef(obj.getInfo(), type));
      LoadStmt load = new LoadStmt(obj.getInfo(), var, new Reference(obj.getInfo(), ptr.getVariable()));
      obj.setLink(var);
      obj.getOffset().clear();
      param.add(ptr);
      param.add(load);
    }
    super.visitReference(obj, param);
    return null;
  }
}


class StackMemTrav extends StatementReplacer<Void> {

  final private Map<Variable, SsaVariable> map = new HashMap<Variable, SsaVariable>();
  private KnowBaseItem kbi;

  public StackMemTrav(KnowledgeBase kb) {
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  public Map<Variable, SsaVariable> getMap() {
    return map;
  }

  @Override
  protected List<NormalStmt> visitVarDef(VarDefStmt obj, Void param) {
    Type type = obj.getVariable().getType().getRef();

    PointerType pt = kbi.getPointerType(type);
    SsaVariable var = new SsaVariable(obj.getVariable().getInfo(), obj.getVariable().getName() + Designator.NAME_SEP + "p", new TypeRef(new ElementInfo(), pt));
    StackMemoryAlloc sma = new StackMemoryAlloc(obj.getInfo(), var);

    map.put(obj.getVariable(), var);

    return ret(sma);
  }

  @Override
  protected List<NormalStmt> visitPhi(PhiStmt phi, BasicBlock in, Void param) {
    return null;
  }
}

class DerefInserterAndRelinker extends DefTraverser<Void, Map<Variable, Variable>> {

  @Override
  protected Void visitReference(Reference obj, Map<Variable, Variable> param) {
    super.visitReference(obj, param);
    if( param.containsKey(obj.getLink()) ) {
      assert ( obj.getLink() instanceof Variable );
      obj.setLink(param.get(obj.getLink()));
      obj.getOffset().add(0, new RefPtrDeref(new ElementInfo()));
    }
    return null;
  }
}
