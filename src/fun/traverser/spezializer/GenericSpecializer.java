package fun.traverser.spezializer;

import java.util.List;

import common.ElementInfo;

import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.reference.ReferenceLinked;
import fun.generator.TypeGenerator;
import fun.knowledge.KnowledgeBase;
import fun.type.NamedType;
import fun.type.Type;
import fun.type.genfunc.Array;
import fun.type.genfunc.GenericArray;
import fun.type.genfunc.GenericTypeType;
import fun.type.genfunc.GenericUnsigned;
import fun.type.genfunc.TypeType;
import fun.type.genfunc.Unsigned;

public class GenericSpecializer extends NullTraverser<Type, List<Expression>> {
  @Override
  protected Type visitDefault(Fun obj, List<Expression> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  public static Type process(TypeGenerator type, List<Expression> genspec, KnowledgeBase kb) {
    GenericSpecializer specializer = new GenericSpecializer();
    return specializer.traverse(type.getItem(), genspec);
  }

  @Override
  protected Type visitGenericUnsigned(GenericUnsigned obj, List<Expression> param) {
    assert (param.size() == 1);
    Expression bits = param.get(0);
    assert (bits instanceof Number);
    return new Unsigned(bits.getInfo(), ((Number) bits).getValue());
  }

  @Override
  protected Type visitGenericArray(GenericArray obj, List<Expression> param) {
    assert (param.size() == 2);
    Expression size = param.get(0);
    Expression type = param.get(1);
    assert (type instanceof NamedType);
    assert (size instanceof Number);
    int count = ((Number) size).getValue();
    NamedType typ = (NamedType) type;
    return new Array(obj.getInfo(), count, new ReferenceLinked(new ElementInfo(), typ));
  }

  @Override
  protected Type visitGenericTypeType(GenericTypeType obj, List<Expression> param) {
    assert (param.size() == 1);
    Expression type = param.get(0);
    assert (type instanceof NamedType);
    NamedType typ = (NamedType) type;
    return new TypeType(obj.getInfo(), new ReferenceLinked(new ElementInfo(), typ));
  }

}
