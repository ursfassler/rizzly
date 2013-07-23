package fun.traverser;

import java.util.ArrayList;

import fun.DefGTraverser;
import fun.expression.Expression;
import fun.expression.reference.RefCompcall;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.generator.Generator;
import fun.other.Namespace;
import fun.variable.Variable;

/**
 * Inserts missing empty braces when referencing a generic function:
 *
 * type TheType = ...
 *
 * var a : TheType;
 *
 *
 * =>
 *
 * var a : TheType{};
 *
 * @author urs
 *
 */
public class GenfuncParamExtender extends DefGTraverser<Void, Void> {

  public static void process(Namespace classes) {
    GenfuncParamExtender genfuncParamExtender = new GenfuncParamExtender();
    genfuncParamExtender.traverse(classes, null);
  }

  @Override
  protected Void visitReferenceUnlinked(ReferenceUnlinked obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, Void param) {
    super.visitReferenceLinked(obj, param);
    if (obj.getLink() instanceof Generator) {
      if (obj.getOffset().isEmpty() || !(obj.getOffset().get(0) instanceof RefCompcall)) {
        obj.getOffset().add(0, new RefCompcall(obj.getInfo(), new ArrayList<Expression>()));
      }
    }
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, Void param) {
    return super.visitVariable(obj, param);
  }

}
