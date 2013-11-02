package cir.traverser;

import java.util.Collection;

import cir.CirBase;
import cir.NullTraverser;
import cir.type.ArrayType;
import cir.type.EnumType;
import cir.type.IntType;
import cir.type.NamedElement;
import cir.type.StringType;
import cir.type.StructType;
import cir.type.Type;
import cir.type.TypeAlias;
import cir.type.UnionType;
import cir.type.VoidType;

public class FpcTypeCollector extends NullTraverser<Void, Collection<Type>> {

  @Override
  protected Void visitDefault(CirBase obj, Collection<Type> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Collection<Type> param) {
    visit(obj.getType(), param);
    return null;
  }

  @Override
  protected Void visitTypeAlias(TypeAlias obj, Collection<Type> param) {
    if (!param.contains(obj)) {
      visit(obj.getRef(), param);
      param.add(obj);
    }
    return null;
  }

  @Override
  protected Void visitVoidType(VoidType obj, Collection<Type> param) {
    return null;
  }

  @Override
  protected Void visitEnumType(EnumType obj, Collection<Type> param) {
    if (!param.contains(obj)) {
      param.add(obj);
    }
    return null;
  }

  @Override
  protected Void visitStructType(StructType obj, Collection<Type> param) {
    if (!param.contains(obj)) {
      visitList(obj.getElements(), param);
      param.add(obj);
    }
    return null;
  }

  @Override
  protected Void visitUnionType(UnionType obj, Collection<Type> param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitArrayType(ArrayType obj, Collection<Type> param) {
    if (!param.contains(obj)) {
      visit(obj.getType(), param);
      param.add(obj);
    }
    return null;
  }

  @Override
  protected Void visitIntType(IntType obj, Collection<Type> param) {
    if (!param.contains(obj)) {
      param.add(obj);
    }
    return null;
  }

  @Override
  protected Void visitStringType(StringType obj, Collection<Type> param) {
    if (!param.contains(obj)) {
      param.add(obj);
    }
    return null;
  }

}
