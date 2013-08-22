package pir.passes;

import java.util.HashMap;
import java.util.Map;

import pir.know.KnowBaseItem;
import pir.know.KnowledgeBase;
import pir.other.Program;
import pir.traverser.Relinker;
import pir.type.BooleanType;
import pir.type.UnsignedType;

/**
 * Replaces boolean type with integer type
 * 
 * @author urs
 * 
 */
public class BooleanReplacer {

  public static void process(Program obj, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);
    UnsignedType i1type = kbi.getUnsignedType(1);
    BooleanType bool = kbi.getBooleanType();
    Map<BooleanType, UnsignedType> map = new HashMap<BooleanType, UnsignedType>();
    map.put(bool, i1type);
    Relinker.process(obj, map);
  }

}
