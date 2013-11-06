package fun.traverser;

import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.ReferenceLinked;
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
  protected Void visitReferenceLinked(ReferenceLinked obj, Memory param) {
    if (obj.getLink() instanceof Generator) {
      Generator gen = (Generator) obj.getLink();
      if (!gen.getTemplateParam().isEmpty()) {
        assert (!obj.getOffset().isEmpty());
        assert (obj.getOffset().get(0) instanceof RefTemplCall);
        ReferenceLinked nr = new ReferenceLinked(obj.getInfo(), obj.getLink());
        nr.getOffset().add(obj.getOffset().pop());
        Named func = EvalTo.any(nr, kb);
        obj.setLink(func);
      }
    }
    return super.visitReferenceLinked(obj, param);
  }
}
