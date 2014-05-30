package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.ExprList;
import evl.expression.Expression;
import evl.expression.NamedElementValue;
import evl.expression.reference.Reference;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.other.Namespace;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnsafeUnionType;
import evl.variable.ConstPrivate;
import evl.variable.Constant;
import evl.variable.StateVariable;

/**
 * Creates a type with data of states for whole state machine
 * 
 * @author urs
 * 
 */
public class StateTypeBuilder extends NullTraverser<RecordType, Designator> {
  public static final String SUB_ENTRY_NAME = Designator.NAME_SEP + "sub";
  public static final String CONST_PREFIX = Designator.NAME_SEP + "INIT" + Designator.NAME_SEP;
  final private Namespace typeSpace;
  final private Map<RecordType, ExprList> initValues = new HashMap<RecordType, ExprList>();
  final private Map<RecordType, Constant> initVar = new HashMap<RecordType, Constant>();

  public StateTypeBuilder(Namespace typeSpace) {
    super();
    this.typeSpace = typeSpace;
  }

  public Map<RecordType, Constant> getInitVar() {
    return initVar;
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

    initValues.put(record, new ExprList(obj.getInfo(), new ArrayList<Expression>()));

    return record;
  }

  public UnsafeUnionType makeUnion(State obj, Designator param) {
    param = new Designator(param, "Sub");
    UnsafeUnionType union = new UnsafeUnionType(obj.getInfo(), param.toString(Designator.NAME_SEP), new ArrayList<NamedElement>());
    typeSpace.add(union);
    return union;
  }

  @Override
  protected RecordType visitStateComposite(StateComposite obj, Designator param) {
    RecordType record = makeRecord(obj, param);
    UnsafeUnionType union = makeUnion(obj, param);

    for (State sub : obj.getItemList(State.class)) {
      Type stype = visit(sub, param);
      NamedElement item = new NamedElement(sub.getInfo(), sub.getName(), new TypeRef(new ElementInfo(), stype));
      union.getElement().add(item);
    }

    ExprList uninit = new ExprList(obj.getInfo(), new ArrayList<Expression>());
    NamedElement initStateElem = union.getElement().find(obj.getInitial().getName());
    Constant initvalue = initVar.get(initStateElem.getType().getRef());
    assert (initvalue != null);
    uninit.getValue().add(new NamedElementValue(obj.getInfo(), obj.getInitial().getName(), new Reference(obj.getInfo(), initvalue)));

    NamedElement sub = new NamedElement(obj.getInfo(), SUB_ENTRY_NAME, new TypeRef(new ElementInfo(), union));
    record.getElement().add(sub);

    ExprList value = initValues.get(record);
    assert (value != null);

    value.getValue().add(new NamedElementValue(obj.getInfo(), SUB_ENTRY_NAME, uninit));

    return record;
  }

  @Override
  protected RecordType visitState(State obj, Designator param) {
    RecordType record = super.visitState(obj, new Designator(param, obj.getName()));

    ExprList value = initValues.get(record);
    assert (value != null);

    for (StateVariable var : obj.getVariable()) {
      NamedElement item = new NamedElement(var.getInfo(), var.getName(), var.getType().copy());
      record.getElement().add(item);
      value.getValue().add(new NamedElementValue(var.getInfo(), var.getName(), var.getDef()));
    }

    Constant init = new ConstPrivate(obj.getInfo(), CONST_PREFIX + record.getName(), new TypeRef(obj.getInfo(), record), value);
    initVar.put(record, init);
    typeSpace.add(init);

    return record;
  }

}
