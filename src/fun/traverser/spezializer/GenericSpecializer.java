package fun.traverser.spezializer;

import java.math.BigInteger;
import java.util.List;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
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
import fun.type.genfunc.GenericRange;
import fun.type.genfunc.GenericTypeType;
import fun.type.genfunc.GenericUnsigned;
import fun.type.genfunc.Range;
import fun.type.genfunc.TypeType;

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
  protected Type visitGenericRange(GenericRange obj, List<Expression> param) {
    assert (param.size() == 2);
    Expression low = param.get(0);
    Expression high = param.get(1);
    assert (low instanceof Number);
    assert (high instanceof Number);
    return new Range(obj.getInfo(), ((Number) low).getValue(), ((Number) high).getValue());
  }

  @Override
  protected Type visitGenericUnsigned(GenericUnsigned obj, List<Expression> param) {
    assert (param.size() == 1);
    Expression bits = param.get(0);
    assert (bits instanceof Number);
    BigInteger val = ((Number) bits).getValue();
    if (val.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
      RError.err(ErrorType.Error, obj.getInfo(), "Value to big");
    }
    BigInteger max = BigInteger.valueOf(2).pow(val.intValue()); // TODO correct?
    max = max.add(BigInteger.valueOf(-1));
    return new Range(obj.getInfo(), BigInteger.ZERO, max);
  }

  @Override
  protected Type visitGenericArray(GenericArray obj, List<Expression> param) {
    assert (param.size() == 2);
    Expression size = param.get(0);
    Expression type = param.get(1);
    assert (type instanceof NamedType);
    assert (size instanceof Number);
    BigInteger count = ((Number) size).getValue();
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
