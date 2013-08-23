package pir.passes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pir.know.KnowBaseItem;
import pir.know.KnowledgeBase;
import pir.other.Program;
import pir.traverser.Relinker;
import pir.type.BooleanType;
import pir.type.IntType;
import pir.type.NoSignType;
import pir.type.SignedType;
import pir.type.Type;
import pir.type.UnsignedType;

/**
 * Replaces unsigned, signed and boolean type with nosign integer type
 * 
 * @author urs
 * 
 */
public class LlvmIntTypeReplacer {

  public static void process(Program obj, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);
    NoSignType i1type = kbi.getNoSignType(1);
    BooleanType bool = kbi.getBooleanType();
    Map<Type, NoSignType> map = new HashMap<Type, NoSignType>();
    map.put(bool, i1type);

    intMapper(new HashSet<Type>(obj.getType()), map, kbi);

    Relinker.process(obj, map);
  }

  private static void intMapper(Set<Type> typeSet, Map<Type, NoSignType> map, KnowBaseItem kbi) {
    for (Type type : typeSet) {
      if ((type instanceof UnsignedType) || (type instanceof SignedType)) {
        NoSignType nst = kbi.getNoSignType(((IntType) type).getBits());
        map.put(type, nst);
      }
    }
  }

}
