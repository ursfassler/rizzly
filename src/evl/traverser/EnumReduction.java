package evl.traverser;

import common.ElementInfo;
import evl.DefTraverser;
import evl.copy.Relinker;
import evl.expression.Expression;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.other.RizzlyProgram;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.Range;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    Map<EnumType, Range> typeMap = new HashMap<EnumType, Range>();

    List<EnumType> enumTypes = prg.getType().getItems(EnumType.class);
    for( EnumType et : enumTypes ) {
      Range rt = kbi.getRangeType(et.getElement().size());
      if( !( prg.getType().getList().contains(rt) ) ) {
        prg.getType().add(rt);
      }
      typeMap.put(et, rt);
    }

    ElemReplacer replacer = new ElemReplacer();
    replacer.traverse(prg, null);

    Relinker.relink(prg, typeMap);

    prg.getType().removeAll(enumTypes);
  }
}

class ElemReplacer extends ExprReplacer<Void> {

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    if( obj.getLink() instanceof EnumType ) {
      assert( obj.getOffset().size() == 1 );
      EnumType type = (EnumType) obj.getLink();
      RefName name = (RefName) obj.getOffset().get(0);
      EnumElement elem = type.find(name.getName());
      assert( elem != null );
      int value = type.getElement().indexOf(elem);
      assert( value >= 0 );
      return new evl.expression.Number(elem.getInfo(), BigInteger.valueOf(value));
    }
    return super.visitReference(obj, param);
  }
}