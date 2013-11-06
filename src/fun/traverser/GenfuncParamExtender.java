package fun.traverser;

import fun.DefTraverser;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
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

// TODO remove this class
public class GenfuncParamExtender extends DefTraverser<Void, Void> {

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
    /*
     * if (obj.getLink() instanceof Generator) { if (obj.getOffset().isEmpty() || !(obj.getOffset().get(0) instanceof
     * RefTemplCall)) { obj.getOffset().add(0, new RefTemplCall(obj.getInfo(), new ArrayList<Expression>())); } }
     */
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, Void param) {
    return super.visitVariable(obj, param);
  }

}
