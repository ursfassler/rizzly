/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package evl.pass.hfsmreduction;

import java.util.HashMap;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import evl.copy.Copy;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateSimple;
import evl.data.expression.NamedValue;
import evl.data.expression.RecordValue;
import evl.data.expression.UnsafeUnionValue;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.type.composed.UnsafeUnionType;
import evl.data.variable.ConstPrivate;
import evl.data.variable.Constant;
import evl.data.variable.StateVariable;
import evl.traverser.NullTraverser;

/**
 * Creates a type with data of states for whole state machine
 *
 * @author urs
 *
 */
public class StateTypeBuilder extends NullTraverser<NamedElement, EvlList<NamedElement>> {
  public static final String SUB_ENTRY_NAME = Designator.NAME_SEP + "sub";
  public static final String CONST_PREFIX = Designator.NAME_SEP + "INIT" + Designator.NAME_SEP;
  final private Map<RecordType, RecordValue> initValues = new HashMap<RecordType, RecordValue>();
  final private Map<RecordType, ConstPrivate> initVar = new HashMap<RecordType, ConstPrivate>();
  final private Map<StateVariable, EvlList<NamedElement>> epath = new HashMap<StateVariable, EvlList<NamedElement>>();
  final private Map<State, RecordType> stateType = new HashMap<State, RecordType>();

  public Map<RecordType, ConstPrivate> getInitVar() {
    return initVar;
  }

  public Map<StateVariable, EvlList<NamedElement>> getEpath() {
    return epath;
  }

  private String getName(Named obj) {
    assert (obj.name.length() > 0);
    return obj.name;
  }

  @Override
  protected NamedElement visitDefault(Evl obj, EvlList<NamedElement> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected NamedElement visitStateSimple(StateSimple obj, EvlList<NamedElement> param) {
    RecordType record = makeRecord(obj);

    NamedElement dataElem = new NamedElement(obj.getInfo(), obj.name, new SimpleRef<Type>(ElementInfo.NO, record));

    param = new EvlList<NamedElement>(param);
    param.add(dataElem);

    addVariables(obj, param, record);

    return dataElem;
  }

  public RecordType makeRecord(State obj) {
    String name = Designator.NAME_SEP + "Data";
    RecordType record = new RecordType(obj.getInfo(), name, new EvlList<NamedElement>());

    obj.item.add(record);
    stateType.put(obj, record);
    initValues.put(record, new RecordValue(obj.getInfo(), new EvlList<NamedValue>(), new SimpleRef<Type>(obj.getInfo(), record)));

    return record;
  }

  public UnsafeUnionType makeUnion(State obj) {
    String name = Designator.NAME_SEP + "Sub";
    UnsafeUnionType union = new UnsafeUnionType(obj.getInfo(), name, new EvlList<NamedElement>());
    obj.item.add(union);
    return union;
  }

  @Override
  protected NamedElement visitStateComposite(StateComposite obj, EvlList<NamedElement> param) {
    // FIXME something does not quite work

    RecordType record = makeRecord(obj);

    NamedElement dataElem = new NamedElement(obj.getInfo(), Designator.NAME_SEP + getName(obj), new SimpleRef<Type>(ElementInfo.NO, record));

    param = new EvlList<NamedElement>(param);
    param.add(dataElem);

    addVariables(obj, param, record);

    // add substates

    UnsafeUnionType union = makeUnion(obj);
    NamedElement subElem = new NamedElement(obj.getInfo(), Designator.NAME_SEP + "sub", new SimpleRef<Type>(ElementInfo.NO, union));
    record.element.add(subElem);

    param.add(subElem);

    NamedElement initStateElem = null;
    for (State sub : obj.item.getItems(State.class)) {
      NamedElement item = visit(sub, param);
      union.element.add(item);

      if (sub == obj.initial.link) {
        assert (initStateElem == null);
        initStateElem = item;
      }
    }
    assert (initStateElem != null);

    // set default state

    Constant initvalue = initVar.get(initStateElem.ref.link);
    assert (initvalue != null);
    NamedValue cont = new NamedValue(obj.getInfo(), getName(obj.initial.link), new Reference(obj.getInfo(), initvalue));
    UnsafeUnionValue uninit = new UnsafeUnionValue(obj.getInfo(), cont, new SimpleRef<Type>(obj.getInfo(), union));

    RecordValue value = initValues.get(record);
    assert (value != null);
    value.value.add(new NamedValue(obj.getInfo(), SUB_ENTRY_NAME, uninit));

    return dataElem;
  }

  private void addVariables(State state, EvlList<NamedElement> param, RecordType type) {
    RecordValue value = initValues.get(type);
    assert (value != null);

    for (StateVariable var : state.item.getItems(StateVariable.class)) {
      NamedElement item = new NamedElement(var.getInfo(), getName(var), Copy.copy(var.type));
      type.element.add(item);
      value.value.add(new NamedValue(var.getInfo(), getName(var), Copy.copy(var.def)));

      EvlList<NamedElement> path = new EvlList<NamedElement>(param);
      path.add(item);
      epath.put(var, path);
    }

    ConstPrivate init = new ConstPrivate(state.getInfo(), CONST_PREFIX + getName(type), new SimpleRef<Type>(state.getInfo(), type), value);
    initVar.put(type, init);
    state.item.add(init);
  }

}
