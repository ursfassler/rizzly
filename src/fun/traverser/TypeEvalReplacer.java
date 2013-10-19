package fun.traverser;

import fun.DefGTraverser;
import fun.Fun;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceLinked;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
import fun.knowledge.KnowledgeBase;
import fun.other.Component;
import fun.other.Interface;
import fun.traverser.spezializer.EvalTo;
import fun.type.Type;
import fun.type.base.TypeAlias;
import fun.type.composed.NamedElement;
import fun.type.template.Array;
import fun.variable.CompUse;
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.IfaceUse;
import fun.variable.StateVariable;
import fun.variable.TemplateParameter;

/**
 * Replaces all types with the evaluated expression:
 * 
 * a : U{3+5} => a : U_8
 * 
 * @author urs
 * 
 */
public class TypeEvalReplacer extends DefGTraverser<Void, Memory> {

  private KnowledgeBase kb;

  public TypeEvalReplacer(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  public Void traverse(Fun obj, Memory param) {
    assert (param != null);
    return super.traverse(obj, param);
  }

  private ReferenceLinked eval(Reference expr, Memory mem) {
    Type type = EvalTo.type((ReferenceLinked) expr, kb);
    return new ReferenceLinked(expr.getInfo(), type);
  }

  // TODO are these handlers needed or can it be done by visitReferenceLinked?
  // what is then RefExecutor.visitTypeGenerator for?
  @Override
  protected Void visitNamedElement(NamedElement obj, Memory param) {
    super.visitNamedElement(obj, param);
    obj.setType(eval(obj.getType(), param));
    return null;
  }

  @Override
  protected Void visitIfaceUse(IfaceUse obj, Memory param) {
    super.visitIfaceUse(obj, param);
    Interface type = EvalTo.iface(obj.getType(), kb);
    ReferenceLinked ref = new ReferenceLinked(obj.getType().getInfo(), type);
    obj.setType(ref);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Memory param) {
    super.visitCompUse(obj, param);
    Component type = EvalTo.comp(obj.getType(), kb);
    ReferenceLinked ref = new ReferenceLinked(obj.getType().getInfo(), type);
    obj.setType(ref);
    return null;
  }

  @Override
  protected Void visitConstPrivate(ConstPrivate obj, Memory param) {
    super.visitConstPrivate(obj, param);
    obj.setType(eval(obj.getType(), param));
    return null;
  }

  @Override
  protected Void visitConstGlobal(ConstGlobal obj, Memory param) {
    super.visitConstGlobal(obj, param);
    obj.setType(eval(obj.getType(), param));
    return null;
  }

  @Override
  protected Void visitFuncVariable(FuncVariable obj, Memory param) {
    super.visitFuncVariable(obj, param);
    obj.setType(eval(obj.getType(), param));
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Memory param) {
    super.visitStateVariable(obj, param);
    obj.setType(eval(obj.getType(), param));
    return null;
  }

  @Override
  protected Void visitCompfuncParameter(TemplateParameter obj, Memory param) {
    super.visitCompfuncParameter(obj, param);
    obj.setType(eval(obj.getType(), param));
    return null;
  }

  @Override
  protected Void visitFunctionHeader(FunctionHeader obj, Memory param) {
    super.visitFunctionHeader(obj, param);
    if (obj instanceof FuncWithReturn) {
      ((FuncWithReturn) obj).setRet(eval(((FuncWithReturn) obj).getRet(), param));
    }
    return null;
  }

  @Override
  protected Void visitArray(Array obj, Memory param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, Memory param) {
    super.visitTypeAlias(obj, param);
    obj.setRef(eval(obj.getRef(), param));
    return null;
  }

}
