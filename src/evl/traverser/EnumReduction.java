package evl.traverser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.copy.Copy;
import evl.copy.Relinker;
import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.RangeType;

/**
 * 
 * @author urs
 */
public class EnumReduction extends DefTraverser<Void, Void> {

  public static void process(RizzlyProgram prg, String debugdir) {
    Namespace root = new Namespace(new ElementInfo(), "!!!");
    root.addAll(prg.getType().getList());
    KnowledgeBase kb = new KnowledgeBase(root, debugdir);
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    Map<EnumType, RangeType> typeMap = new HashMap<EnumType, RangeType>();

    List<EnumType> enumTypes = prg.getType().getItems(EnumType.class);
    for (EnumType et : enumTypes) {
      RangeType rt = kbi.getRangeType(et.getElement().size());
      if (!(prg.getType().getList().contains(rt))) {
        prg.getType().add(rt);
      }
      typeMap.put(et, rt);
    }

    ElemReplacer replacer = new ElemReplacer();
    replacer.traverse(prg, null);

    Relinker.relink(prg, typeMap);

    prg.getType().removeAll(enumTypes);
    prg.getConstant().removeAll( prg.getConstant().getItems(EnumElement.class) );  //FIXME hacky, dependency cleaner should find it
  }
}

// TODO make a constant propagation out of it
class ElemReplacer extends ExprReplacer<Void> {

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    if (obj.getLink() instanceof EnumType) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "should not happen: " + obj);
    }
    if (obj.getLink() instanceof EnumElement) {
      EnumElement elem = (EnumElement) obj.getLink();
      return Copy.copy(elem.getDef());
    }
    return super.visitReference(obj, param);
  }
}
