package evl.passes;

import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;
import common.NameFactory;

import evl.DefTraverser;
import evl.copy.Relinker;
import evl.expression.reference.Reference;
import evl.hfsm.Transition;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.statement.normal.TypeCast;
import evl.traverser.range.RangeGetter;
import evl.type.TypeRef;
import evl.type.base.Range;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;

/**
 * Checks if the transition guard narrows a state variable. If so, a new
 * (cached) version is created with the narrow range and references to the state
 * variable are rpelaced.
 * @author urs
 */
public class TransitionGuardNarrower extends DefTraverser<Void, Void> {

  private final KnowledgeBase kb;
  private ElementInfo info = new ElementInfo();

  public TransitionGuardNarrower(KnowledgeBase kb) {
    this.kb = kb;
  }

  public static void process(Namespace aclasses, KnowledgeBase kb) {
    TransitionGuardNarrower narrower = new TransitionGuardNarrower(kb);
    narrower.traverse(aclasses, null);
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    RangeGetter getter = new RangeGetter(kb);
    getter.traverse(obj.getGuard(), null);
    Map<StateVariable, Range> varSRange = getter.getSranges();

    for( StateVariable sv : varSRange.keySet() ) {
      Range rt = varSRange.get(sv);
      SsaVariable ssa = new SsaVariable(info, NameFactory.getNew(), new TypeRef(info, rt));
      TypeCast init = new TypeCast(info, ssa, new TypeRef(info, rt), new Reference(info, sv));
      Map<StateVariable, SsaVariable> map = new HashMap<StateVariable, SsaVariable>();
      map.put(sv, ssa);
      Relinker.relink(obj.getBody().getEntry(), map);  //FIXME how can we ensure that we only replace the one used reference?
      obj.getBody().getEntry().getCode().add(0, init);
    }
    return super.visitTransition(obj, param);
  }
}
