package fun.traverser.spezializer;

import java.util.Map;

import fun.expression.Expression;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.Reference;
import fun.other.ActualTemplateArgument;
import fun.traverser.ExprReplacer;
import fun.type.Type;
import fun.variable.TemplateParameter;

/**
 * Replaces a reference to a CompfuncParameter with the value of it
 * 
 * @author urs
 * 
 */
public class TypeSpecTrav extends ExprReplacer<Map<TemplateParameter, ActualTemplateArgument>> {

  @Override
  protected Expression visitReference(Reference obj, Map<TemplateParameter, ActualTemplateArgument> param) {
    assert (!(obj.getLink() instanceof DummyLinkTarget));
    super.visitReference(obj, param);

    if (param.containsKey(obj.getLink())) {
      ActualTemplateArgument repl = param.get(obj.getLink());
      if (repl instanceof Type) {
        return new Reference(obj.getInfo(), (Type) repl);
      } else {
        return (Expression) repl;
      }
    } else {
      assert (!(obj.getLink() instanceof TemplateParameter));
      return obj;
    }
  }

}
