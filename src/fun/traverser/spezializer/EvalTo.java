package fun.traverser.spezializer;

import java.util.List;

import fun.expression.Expression;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceLinked;
import fun.generator.ComponentGenerator;
import fun.generator.InterfaceGenerator;
import fun.generator.TypeGenerator;
import fun.knowledge.KnowledgeBase;
import fun.other.NamedComponent;
import fun.other.NamedInterface;
import fun.type.NamedType;

public class EvalTo {
  public static NamedInterface iface(Reference expr, KnowledgeBase kb) {
    assert (expr instanceof ReferenceLinked);

    ReferenceLinked obj = (ReferenceLinked) expr;

    assert (obj.getLink() instanceof InterfaceGenerator);
    assert (obj.getOffset().size() == 1);
    assert (obj.getOffset().get(0) instanceof RefTemplCall);

    InterfaceGenerator generator = (InterfaceGenerator) obj.getLink();
    List<Expression> actparam = ((RefTemplCall) obj.getOffset().get(0)).getActualParameter();

    return Specializer.processIface(generator, actparam, expr.getInfo(), kb);
  }

  public static NamedComponent comp(Reference expr, KnowledgeBase kb) {
    assert (expr instanceof ReferenceLinked);

    ReferenceLinked obj = (ReferenceLinked) expr;

    assert (obj.getLink() instanceof ComponentGenerator);
    assert (obj.getOffset().size() == 1);
    assert (obj.getOffset().get(0) instanceof RefTemplCall);

    ComponentGenerator generator = (ComponentGenerator) obj.getLink();
    List<Expression> actparam = ((RefTemplCall) obj.getOffset().get(0)).getActualParameter();

    return Specializer.processComp(generator, actparam, expr.getInfo(), kb);
  }

  public static NamedType type(Reference expr, KnowledgeBase kb) {
    
    assert (expr instanceof ReferenceLinked);

    ReferenceLinked obj = (ReferenceLinked) expr;

    if( obj.getLink() instanceof NamedType ){
      return (NamedType) obj.getLink();
    }
    assert (obj.getLink() instanceof TypeGenerator);
    assert (obj.getOffset().size() == 1);
    assert (obj.getOffset().get(0) instanceof RefTemplCall);

    TypeGenerator generator = (TypeGenerator) obj.getLink();
    List<Expression> actparam = ((RefTemplCall) obj.getOffset().get(0)).getActualParameter();

    return Specializer.processType(generator, actparam, expr.getInfo(), kb);
  }
}
