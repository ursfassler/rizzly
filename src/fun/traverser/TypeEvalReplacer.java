package fun.traverser;

import fun.DefGTraverser;
import fun.Fun;
import fun.expression.Expression;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.ReferenceLinked;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
import fun.generator.TypeGenerator;
import fun.knowledge.KnowledgeBase;
import fun.other.NamedComponent;
import fun.other.NamedInterface;
import fun.traverser.spezializer.EvalTo;
import fun.traverser.spezializer.ExprEvaluator;
import fun.traverser.spezializer.Specializer;
import fun.type.NamedType;
import fun.type.base.EnumType;
import fun.type.base.TypeAlias;
import fun.type.composed.NamedElement;
import fun.type.template.Array;
import fun.variable.CompUse;
import fun.variable.TemplateParameter;
import fun.variable.ConstGlobal;
import fun.variable.ConstPrivate;
import fun.variable.FuncVariable;
import fun.variable.IfaceUse;
import fun.variable.StateVariable;

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

  private ReferenceLinked eval(Expression expr, Memory mem) {
    NamedType type = (NamedType) ExprEvaluator.evaluate(expr, mem, kb);
    ReferenceLinked ref = new ReferenceLinked(expr.getInfo(), type);
    return ref;
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
    NamedInterface type = EvalTo.iface(obj.getType(), kb);
    ReferenceLinked ref = new ReferenceLinked(obj.getType().getInfo(), type);
    obj.setType(ref);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Memory param) {
    super.visitCompUse(obj, param);
    NamedComponent type = EvalTo.comp(obj.getType(), kb);
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

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, Memory param) {
    super.visitReferenceLinked(obj, param);

    // this is only necessary because Enumeration types are references from within the code since the type ref is needed
    // to specifiy an element; i.e. Weekday.Monday
    // TODO remove this hack
    if ((obj.getLink() instanceof TypeGenerator) && (((TypeGenerator) obj.getLink()).getItem() instanceof EnumType) && !obj.getOffset().isEmpty() && (obj.getOffset().get(0) instanceof RefTemplCall)) {
      assert (obj.getLink() instanceof TypeGenerator);
      RefTemplCall call = (RefTemplCall) obj.getOffset().pop();
      assert (call.getActualParameter().isEmpty());
      NamedType type = Specializer.processType((TypeGenerator) obj.getLink(), call.getActualParameter(), kb);
      assert (type.getType() instanceof EnumType);
      obj.setLink(type);
    }
    return null;
  }

}
