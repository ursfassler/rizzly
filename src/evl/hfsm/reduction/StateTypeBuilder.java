package evl.hfsm.reduction;

import java.util.ArrayList;

import common.Designator;

import common.ElementInfo;
import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.other.Namespace;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionSelector;
import evl.type.composed.UnionType;
import evl.variable.Variable;

/**
 * Creates a type with data of states for whole state machine
 *
 * @author urs
 *
 */
public class StateTypeBuilder extends NullTraverser<RecordType, Designator> {
  public static final String SUB_ENTRY_NAME = "sub";
  final private Namespace typeSpace;

  public StateTypeBuilder(Namespace typeSpace) {
    super();
    this.typeSpace = typeSpace;
  }

  @Override
  protected RecordType visitDefault(Evl obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected RecordType visitStateSimple(StateSimple obj, Designator param) {
    return makeRecord(obj, param);
  }

  public RecordType makeRecord(State obj, Designator param) {
    param = new Designator(param, "Data");
    RecordType record = new RecordType(obj.getInfo(), param.toString(Designator.NAME_SEP), new ArrayList<NamedElement>());
    typeSpace.add(record);
    return record;
  }

  public UnionType makeUnion(State obj, Designator param) {
    param = new Designator(param, "Sub");
    UnionType union = new UnionType(obj.getInfo(), param.toString(Designator.NAME_SEP), new UnionSelector(obj.getInfo(), "state"), new ArrayList<NamedElement>());
    typeSpace.add(union);
    return union;
  }

  @Override
  protected RecordType visitStateComposite(StateComposite obj, Designator param) {
    RecordType record = makeRecord(obj, param);
    UnionType union = makeUnion(obj, param);

    for (State sub : obj.getItemList(State.class)) {
      Type stype = visit(sub, param);
      NamedElement item = new NamedElement(sub.getInfo(), sub.getName(), new TypeRef(new ElementInfo(), stype) );
      union.getElement().add(item);
    }

    NamedElement sub = new NamedElement(obj.getInfo(), SUB_ENTRY_NAME,  new TypeRef(new ElementInfo(),union));
    record.getElement().add(sub);

    return record;
  }

  @Override
  protected RecordType visitState(State obj, Designator param) {
    RecordType record = super.visitState(obj, new Designator(param, obj.getName()));

    for (Variable var : obj.getVariable()) {
      NamedElement item = new NamedElement(var.getInfo(), var.getName(), var.getType());
      record.getElement().add(item);
    }

    return record;
  }

}
