package evl.traverser.range;

import error.ErrorType;
import error.RError;
import java.util.List;
import java.util.Map;

import evl.Evl;
import evl.NullTraverser;
import evl.statement.bbend.CaseOptEntry;
import evl.statement.bbend.CaseOptRange;
import evl.statement.bbend.CaseOptValue;
import evl.expression.Expression;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.Range;
import evl.variable.Variable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author urs
 */
public class CaseEnumUpdater extends NullTraverser<Void, Map<Variable, Range>> {

  final private Set<EnumElement> elements = new HashSet<EnumElement>();
  final private Set<EnumType> types = new HashSet<EnumType>();
  final private KnowBaseItem kbi;

  public CaseEnumUpdater(KnowledgeBase kb) {
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static EnumType process(List<CaseOptEntry> values, KnowledgeBase kb) {
    assert ( !values.isEmpty() );
    CaseEnumUpdater updater = new CaseEnumUpdater(kb);
    updater.visitItr(values, null);
    return updater.getType();
  }

  private EnumType getType() {
    assert ( types.size() == 1 );
    EnumType type = types.iterator().next();
    ArrayList<EnumElement> sorted = new ArrayList<EnumElement>(type.getElement());
    sorted.retainAll(elements);
    return kbi.getEnumType(type, sorted);
  }

  @Override
  protected Void visitDefault(Evl obj, Map<Variable, Range> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitCaseOptRange(CaseOptRange obj, Map<Variable, Range> param) {
    EnumElement low = evalToElem(obj.getStart());
    EnumElement high = evalToElem(obj.getEnd());
    assert ( types.size() == 1 );
    EnumType type = types.iterator().next();

    int start = type.getElement().indexOf(low);
    int end = type.getElement().indexOf(high);
    assert ( start <= end );

    for( int i = start; i <= end; i++ ) {
      EnumElement elem = type.getElement().get(i);
      elements.add(elem);
    }

    return null;
  }

  @Override
  protected Void visitCaseOptValue(CaseOptValue obj, Map<Variable, Range> param) {
    EnumElement elem = evalToElem(obj.getValue());
    elements.add(elem);
    return null;
  }

  private EnumElement evalToElem(Expression value) {
    Reference ref = (Reference) value;
    assert ( ref.getOffset().size() == 1 );
    EnumType type = (EnumType) ref.getLink();
    RefName name = (RefName) ref.getOffset().get(0);
    EnumElement elem = type.find(name.getName());
    assert ( elem != null );
    types.add(type);
    return elem;
  }
}
