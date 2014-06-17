package fun.toevl;

import evl.Evl;
import evl.other.Component;
import evl.type.Type;
import evl.type.TypeRef;
import evl.variable.Variable;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.variable.CompUse;
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.StateVariable;

public class FunToEvlVariable extends NullTraverser<Evl, Void> {
  private FunToEvl fta;

  public FunToEvlVariable(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  @Override
  protected Variable visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------

  private TypeRef copyType(Expression typeRef) {
    Reference typeref = (Reference) typeRef;
    fun.type.Type nt = FunToEvl.getRefType(typeref);
    Type ecomp = (Type) fta.traverse(nt, null);
    return new TypeRef(typeref.getInfo(), ecomp);
  }

  @Override
  protected Evl visitFuncVariable(FuncVariable obj, Void param) {
    return new evl.variable.FuncVariable(obj.getInfo(), obj.getName(), copyType(obj.getType()));
  }

  @Override
  protected Evl visitStateVariable(StateVariable obj, Void param) {
    return new evl.variable.StateVariable(obj.getInfo(), obj.getName(), copyType(obj.getType()), (evl.expression.Expression) fta.traverse(obj.getDef(), null));
  }

  @Override
  protected Evl visitConstPrivate(ConstPrivate obj, Void param) {
    return new evl.variable.ConstPrivate(obj.getInfo(), obj.getName(), copyType(obj.getType()), (evl.expression.Expression) fta.traverse(obj.getDef(), null));
  }

  @Override
  protected Evl visitConstGlobal(ConstGlobal obj, Void param) {
    return new evl.variable.ConstGlobal(obj.getInfo(), obj.getName(), copyType(obj.getType()), (evl.expression.Expression) fta.traverse(obj.getDef(), null));
  }

  @Override
  protected Evl visitCompUse(CompUse obj, Void param) {
    Reference typeref = (Reference) obj.getType();
    assert (typeref.getOffset().isEmpty());
    fun.other.Component nt = (fun.other.Component) typeref.getLink();
    Component ecomp = (Component) fta.traverse(nt, null);
    return new evl.other.CompUse(obj.getInfo(), obj.getName(), ecomp);
  }

}
