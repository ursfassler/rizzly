package fun.traverser;

import java.util.ArrayList;
import java.util.List;

import fun.DefTraverser;
import fun.expression.Expression;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.knowledge.KnowledgeBase;
import fun.other.Generator;
import fun.other.Named;
import fun.traverser.spezializer.Specializer;
import fun.variable.ConstGlobal;

//TODO: merge with Specializer?

/**
 * Replaces all types with the evaluated expression:
 * 
 * a : U{3+5} => a : U_8
 * 
 * @author urs
 * 
 */
public class TypeEvalReplacer extends DefTraverser<Void, Void> {

  final private KnowledgeBase kb;

  public TypeEvalReplacer(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    super.visitReference(obj, param);
    if (obj.getLink() instanceof Generator) {
      List<Expression> arg = new ArrayList<>();
      if (!obj.getOffset().isEmpty()) {
        if (obj.getOffset().get(0) instanceof RefTemplCall) {
          arg = ((RefTemplCall) obj.getOffset().pop()).getActualParameter();
        }
      }

      Named inst = Specializer.process((Generator) obj.getLink(), arg, obj.getInfo(), kb);
      obj.setLink(inst);
    } else if ((obj.getLink() instanceof ConstGlobal)) {
      // FIXME this is a temporary workaround, the copy should be done in the instantiater (Specializer?)
      visit(obj.getLink(), null);   // somebody has to instantiate the constant
    }
    return null;
  }
}
