package evl.traverser.typecheck;

import evl.Evl;
import evl.NullTraverser;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.EnumType;
import evl.type.base.TypeAlias;
import evl.type.base.Unsigned;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;

public class Supertype extends NullTraverser<Type, Void> {
  private KnowBaseItem kbi;

  public Supertype(KnowledgeBase kb) {
    super();
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  static public Type get(Type typ, KnowledgeBase kb) {
    Supertype supertype = new Supertype(kb);
    return supertype.traverse(typ, null);
  }

  @Override
  protected Type visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitUnsigned(Unsigned obj, Void param) {
    return kbi.getNaturalType();
  }

  @Override
  protected Type visitRecordType(RecordType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitUnionType(UnionType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitEnumType(EnumType obj, Void param) {
    return kbi.getNaturalType();
  }

  @Override
  protected Type visitVoidType(VoidType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitTypeAlias(TypeAlias obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitBooleanType(BooleanType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitNaturalType(NaturalType obj, Void param) {
    return kbi.getIntegerType();
  }

}
