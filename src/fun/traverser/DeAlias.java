package fun.traverser;

import fun.DefGTraverser;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceLinked;
import fun.other.Namespace;
import fun.type.NamedType;
import fun.type.base.TypeAlias;

/**
 * Removes the alias type by type declaration forwarding (i.e. replaces references to alias types with the type in the alias type.
 * There ar eno more alias types after this step.
 *   
 * @author urs
 */
public class DeAlias extends DefGTraverser<Void, Void> {

  public static void process(Namespace classes) {
    DeAlias deAlias = new DeAlias();
    deAlias.traverse(classes, null);
  }

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, Void param) {
    while( ( obj.getLink() instanceof NamedType ) && ( ( (NamedType) obj.getLink() ).getType() instanceof TypeAlias ) ) {
      assert ( obj.getOffset().isEmpty() );
      Reference ref = ( (TypeAlias) ( (NamedType) obj.getLink() ).getType() ).getRef();
      assert ( ref instanceof ReferenceLinked );
      assert ( ref.getOffset().isEmpty() );
      obj.setLink(( (ReferenceLinked) ref ).getLink());
    }
    return super.visitReferenceLinked(obj, param);
  }
}