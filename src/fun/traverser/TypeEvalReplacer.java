package fun.traverser;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.knowledge.KnowledgeBase;
import fun.other.Generator;
import fun.other.Named;
import fun.traverser.spezializer.EvalTo;

/**
 * Replaces all types with the evaluated expression:
 * 
 * a : U{3+5} => a : U_8
 * 
 * @author urs
 * 
 */
public class TypeEvalReplacer extends DefTraverser<Void, Memory> {

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

  @Override
  protected Void visitReference(Reference obj, Memory param) {
    if (obj.getLink() instanceof Generator) {
      Generator gen = (Generator) obj.getLink();

      if (!gen.getTemplateParam().isEmpty()) {
        if ((obj.getOffset().isEmpty()) || !(obj.getOffset().get(0) instanceof RefTemplCall)) {
          RError.err(ErrorType.Error, obj.getInfo(), "Missing arguments");
        }
        Reference nr = new Reference(obj.getInfo(), obj.getLink());
        nr.getOffset().add(obj.getOffset().pop());
        Named func = EvalTo.any(nr, kb);
        obj.setLink(func);
      }
    }
    return super.visitReference(obj, param);
  }
}
