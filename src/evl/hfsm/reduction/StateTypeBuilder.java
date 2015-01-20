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

package evl.hfsm.reduction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.copy.Copy;
import evl.expression.NamedElementValue;
import evl.expression.RecordValue;
import evl.expression.UnsafeUnionValue;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.knowledge.KnowParent;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Named;
import evl.other.Namespace;
import evl.type.Type;
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
public class StateTypeBuilder extends NullTraverser<NamedElement, EvlList<NamedElement>> {
  public static final String SUB_ENTRY_NAME = Designator.NAME_SEP + "sub";
  public static final String CONST_PREFIX = Designator.NAME_SEP + "INIT" + Designator.NAME_SEP;
  final private Namespace typeSpace;
  final private KnowParent kp;
  final private Map<RecordType, RecordValue> initValues = new HashMap<RecordType, RecordValue>();
  final private Map<RecordType, Constant> initVar = new HashMap<RecordType, Constant>();
  final private Map<StateVariable, EvlList<NamedElement>> epath = new HashMap<StateVariable, EvlList<NamedElement>>();

  public StateTypeBuilder(Namespace typeSpace, KnowledgeBase kb) {
    super();
    this.typeSpace = typeSpace;
    kp = kb.getEntry(KnowParent.class);
  }

  public Map<RecordType, Constant> getInitVar() {
    return initVar;
  }

  public Map<StateVariable, EvlList<NamedElement>> getEpath() {
    return epath;
  }

  private String getName(Named obj) {
    assert (obj.getName().length() > 0);
    return obj.getName();
  }

  // workaround to make sure that records do not have the same name if sustates have same names
  private String getFullName(Named obj) {
    LinkedList<String> full = new LinkedList<String>();
    for (Evl evl = obj; evl instanceof State; evl = kp.getParent(evl)) {
      full.addFirst(((State) evl).getName());
    }

    return (new Designator(full)).toString(Designator.NAME_SEP);
  }

  @Override
  protected NamedElement visitDefault(Evl obj, EvlList<NamedElement> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected NamedElement visitStateSimple(StateSimple obj, EvlList<NamedElement> param) {
    RecordType record = makeRecord(obj);

    NamedElement dataElem = new NamedElement(obj.getInfo(), obj.getName(), new SimpleRef<Type>(ElementInfo.NO, record));

    param = new EvlList<NamedElement>(param);
    param.add(dataElem);

    addVariables(obj, param, record);

    return dataElem;
  }

  public RecordType makeRecord(State obj) {
    String name = getFullName(obj);
    name = Designator.NAME_SEP + name + Designator.NAME_SEP + "Data";
    RecordType record = new RecordType(obj.getInfo(), name, new EvlList<NamedElement>());
    typeSpace.add(record);

    initValues.put(record, new RecordValue(obj.getInfo(), new EvlList<NamedElementValue>(), new SimpleRef<Type>(obj.getInfo(), record)));

    return record;
  }

  public UnsafeUnionType makeUnion(State obj) {
    String name = getFullName(obj);
    name = Designator.NAME_SEP + name + Designator.NAME_SEP + "Sub";
    UnsafeUnionType union = new UnsafeUnionType(obj.getInfo(), name, new EvlList<NamedElement>());
    typeSpace.add(union);
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
    record.getElement().add(subElem);

    param.add(subElem);

    NamedElement initStateElem = null;
    for (State sub : obj.getItem().getItems(State.class)) {
      NamedElement item = visit(sub, param);
      union.getElement().add(item);

      if (sub == obj.getInitial().getLink()) {
        assert (initStateElem == null);
        initStateElem = item;
      }
    }
    assert (initStateElem != null);

    // set default state

    Constant initvalue = initVar.get(initStateElem.getRef().getLink());
    assert (initvalue != null);
    NamedElementValue cont = new NamedElementValue(obj.getInfo(), getName(obj.getInitial().getLink()), new Reference(obj.getInfo(), initvalue));
    UnsafeUnionValue uninit = new UnsafeUnionValue(obj.getInfo(), cont, new SimpleRef<Type>(obj.getInfo(), union));

    RecordValue value = initValues.get(record);
    assert (value != null);
    value.getValue().add(new NamedElementValue(obj.getInfo(), SUB_ENTRY_NAME, uninit));

    return dataElem;
  }

  private void addVariables(State state, EvlList<NamedElement> param, RecordType type) {
    RecordValue value = initValues.get(type);
    assert (value != null);

    for (StateVariable var : state.getItem().getItems(StateVariable.class)) {
      NamedElement item = new NamedElement(var.getInfo(), getName(var), Copy.copy(var.getType()));
      type.getElement().add(item);
      value.getValue().add(new NamedElementValue(var.getInfo(), getName(var), Copy.copy(var.getDef())));

      EvlList<NamedElement> path = new EvlList<NamedElement>(param);
      path.add(item);
      epath.put(var, path);
    }

    Constant init = new ConstPrivate(state.getInfo(), CONST_PREFIX + getName(type), new SimpleRef<Type>(state.getInfo(), type), value);
    initVar.put(type, init);
    typeSpace.add(init);
  }

}
