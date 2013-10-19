package fun.toevl;

import java.util.Map;

import evl.Evl;
import evl.other.Component;
import evl.other.Interface;
import evl.type.Type;
import evl.type.TypeRef;
import evl.variable.Variable;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.reference.ReferenceLinked;
import fun.type.base.EnumElement;
import fun.variable.CompUse;
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.IfaceUse;
import fun.variable.StateVariable;

public class FunToEvlVariable extends NullTraverser<Evl, Void> {
  private Map<Fun, Evl> map;
  private FunToEvl fta;

  public FunToEvlVariable(FunToEvl fta, Map<Fun, Evl> map) {
    super();
    this.map = map;
    this.fta = fta;
  }

  @Override
  protected Evl visit(Fun obj, Void param) {
    Evl cobj = (Evl) map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected Variable visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------

  private TypeRef copyType(Expression typeRef) {
    ReferenceLinked typeref = (ReferenceLinked) typeRef;
    assert (typeref.getOffset().isEmpty());
    fun.type.Type nt = (fun.type.Type) typeref.getLink();
    Type ecomp = (Type) fta.traverse(nt, null);
    return new TypeRef(typeref.getInfo(), ecomp);
  }

  @Override
  protected Evl visitFuncVariable(FuncVariable obj, Void param) {
    return new evl.variable.FuncVariable(obj.getInfo(), obj.getName(), copyType(obj.getType()));
  }

  @Override
  protected Evl visitStateVariable(StateVariable obj, Void param) {
    return new evl.variable.StateVariable(obj.getInfo(), obj.getName(), copyType(obj.getType()));
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
  protected Evl visitEnumElement(EnumElement obj, Void param) {
    Number num = (Number) obj.getDef();
    return new evl.type.base.EnumElement(obj.getInfo(), obj.getName(), copyType(obj.getType()), num.getValue());
  }

  @Override
  protected Evl visitCompUse(CompUse obj, Void param) {
    ReferenceLinked typeref = (ReferenceLinked) obj.getType();
    assert (typeref.getOffset().isEmpty());
    fun.other.Component nt = (fun.other.Component) typeref.getLink();
    Component ecomp = (Component) fta.traverse(nt, null);
    return new evl.other.CompUse(obj.getInfo(), obj.getName(), ecomp);
  }

  @Override
  protected Evl visitIfaceUse(IfaceUse obj, Void param) {
    ReferenceLinked typeref = (ReferenceLinked) obj.getType();
    assert (typeref.getOffset().isEmpty());
    fun.other.Interface nt = (fun.other.Interface) typeref.getLink();
    Interface ecomp = (Interface) fta.traverse(nt, null);
    return new evl.other.IfaceUse(obj.getInfo(), obj.getName(), ecomp);
  }

}
