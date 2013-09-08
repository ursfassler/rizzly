package evl.passes;

import common.NameFactory;
import evl.DefTraverser;
import evl.copy.Copy;
import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.statement.Assignment;
import evl.statement.GetElementPtr;
import evl.statement.LoadStmt;
import evl.statement.Statement;
import evl.statement.StoreStmt;
import evl.traverser.StatementReplacer;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.special.PointerType;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;
import evl.variable.Variable;
import java.util.ArrayList;
import java.util.List;
import util.ssa.PhiInserter;

/**
 * Inserts a load statement for every read access on memory and a store statement for every write access.
 * @author urs
 */
public class MemoryAccessCapsulater {

  public static void process(Namespace aclasses, KnowledgeBase kb) {
    StmtReplacer loadInserter = new StmtReplacer(kb);
    loadInserter.traverse(aclasses, null);
  }
}

class StmtReplacer extends StatementReplacer<List<Statement>> {

  private KnowledgeBase kb;
  ExprReplacer exprReplacer;

  public StmtReplacer(KnowledgeBase kb) {
    this.kb = kb;
    exprReplacer = new ExprReplacer(kb);
  }

  @Override
  protected List<Statement> visitStatement(Statement obj, List<Statement> param) {
    assert ( param == null );
    param = new ArrayList<Statement>();
    List<Statement> ret = super.visitStatement(obj, param);
    assert ( ret == null );
    if( !( obj instanceof Assignment ) ) {  // hacky
      param.add(obj);
    }
    return param;
  }

  @Override
  protected List<Statement> visitGetElementPtr(GetElementPtr obj, List<Statement> param) {
    return null;
  }

  @Override
  protected List<Statement> visitExpression(Expression obj, List<Statement> param) {
    exprReplacer.traverse(obj, param);
    return null;
  }

  @Override
  protected List<Statement> visitAssignment(Assignment obj, List<Statement> param) {
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
}

class ExprReplacer extends DefTraverser<Void, List<Statement>> {

  private KnowledgeBase kb;

  public ExprReplacer(KnowledgeBase kb) {
    this.kb = kb;
  }

  static boolean isMemoryAccess(Reference ref) {
    if( ref.getLink() instanceof Variable ) {
      Variable var = (Variable) ref.getLink();
      if( ( var instanceof StateVariable ) || !PhiInserter.isScalar(var.getType().getRef()) ) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected Void visitGetElementPtr(GetElementPtr obj, List<Statement> param) {
    return null;
  }

  static GetElementPtr makeGep(Reference obj, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    Type type = ExpressionTypeChecker.process(obj, kb);

    PointerType pt = kbi.getPointerType(new TypeRef(obj.getInfo(), type));
    SsaVariable addr = new SsaVariable(obj.getInfo(), NameFactory.getNew(), new TypeRef(obj.getInfo(), pt));
    GetElementPtr ptr = new GetElementPtr(obj.getInfo(), addr, Copy.copy(obj));
    return ptr;
  }

  @Override
  protected Void visitReference(Reference obj, List<Statement> param) {
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