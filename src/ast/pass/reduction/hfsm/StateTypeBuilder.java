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

package ast.pass.reduction.hfsm;

import java.util.HashMap;
import java.util.Map;

import ast.Designator;
import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Named;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateSimple;
import ast.data.expression.RefExp;
import ast.data.expression.value.NamedValue;
import ast.data.expression.value.RecordValue;
import ast.data.expression.value.UnsafeUnionValue;
import ast.data.reference.RefFactory;
import ast.data.type.TypeRefFactory;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.variable.ConstPrivate;
import ast.data.variable.Constant;
import ast.data.variable.StateVariable;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.repository.query.TypeFilter;
import ast.traverser.NullTraverser;

/**
 * Creates a type with data of states for whole state machine
 *
 * @author urs
 *
 */
public class StateTypeBuilder extends NullTraverser<NamedElement, AstList<NamedElement>> {
  public static final String SUB_ENTRY_NAME = Designator.NAME_SEP + "sub";
  public static final String CONST_PREFIX = Designator.NAME_SEP + "INIT" + Designator.NAME_SEP;
  final private Map<RecordType, RecordValue> initValues = new HashMap<RecordType, RecordValue>();
  final private Map<RecordType, ConstPrivate> initVar = new HashMap<RecordType, ConstPrivate>();
  final private Map<StateVariable, AstList<NamedElement>> epath = new HashMap<StateVariable, AstList<NamedElement>>();
  final private Map<State, RecordType> stateType = new HashMap<State, RecordType>();

  final private KnowType kt;

  public StateTypeBuilder(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  public Map<RecordType, ConstPrivate> getInitVar() {
    return initVar;
  }

  public Map<StateVariable, AstList<NamedElement>> getEpath() {
    return epath;
  }

  private String getName(Named obj) {
    assert (obj.name.length() > 0);
    return obj.name;
  }

  @Override
  protected NamedElement visitDefault(Ast obj, AstList<NamedElement> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected NamedElement visitStateSimple(StateSimple obj, AstList<NamedElement> param) {
    RecordType record = makeRecord(obj);

    NamedElement dataElem = new NamedElement(obj.getInfo(), obj.name, TypeRefFactory.create(ElementInfo.NO, record));

    param = new AstList<NamedElement>(param);
    param.add(dataElem);

    addVariables(obj, param, record);

    return dataElem;
  }

  public RecordType makeRecord(State obj) {
    String name = Designator.NAME_SEP + "Data";
    RecordType record = new RecordType(obj.getInfo(), name, new AstList<NamedElement>());

    obj.item.add(record);
    stateType.put(obj, record);
    initValues.put(record, new RecordValue(obj.getInfo(), new AstList<NamedValue>(), TypeRefFactory.create(obj.getInfo(), record)));

    return record;
  }

  public UnsafeUnionType makeUnion(State obj) {
    String name = Designator.NAME_SEP + "Sub";
    UnsafeUnionType union = new UnsafeUnionType(obj.getInfo(), name, new AstList<NamedElement>());
    obj.item.add(union);
    return union;
  }

  @Override
  protected NamedElement visitStateComposite(StateComposite obj, AstList<NamedElement> param) {
    // FIXME something does not quite work

    RecordType record = makeRecord(obj);

    NamedElement dataElem = new NamedElement(obj.getInfo(), Designator.NAME_SEP + getName(obj), TypeRefFactory.create(ElementInfo.NO, record));

    param = new AstList<NamedElement>(param);
    param.add(dataElem);

    addVariables(obj, param, record);

    // add substates

    UnsafeUnionType union = makeUnion(obj);
    NamedElement subElem = new NamedElement(obj.getInfo(), Designator.NAME_SEP + "sub", TypeRefFactory.create(ElementInfo.NO, union));
    record.element.add(subElem);

    param.add(subElem);

    NamedElement initStateElem = null;
    for (State sub : TypeFilter.select(obj.item, State.class)) {
      NamedElement item = visit(sub, param);
      union.element.add(item);

      if (sub == obj.initial.getTarget()) {
        assert (initStateElem == null);
        initStateElem = item;
      }
    }
    assert (initStateElem != null);

    // set default state

    Constant initvalue = initVar.get(kt.get(initStateElem.typeref));
    assert (initvalue != null);
    NamedValue cont = new NamedValue(obj.getInfo(), getName(obj.initial.getTarget()), new RefExp(obj.getInfo(), RefFactory.full(obj.getInfo(), initvalue)));
    UnsafeUnionValue uninit = new UnsafeUnionValue(obj.getInfo(), cont, TypeRefFactory.create(obj.getInfo(), union));

    RecordValue value = initValues.get(record);
    assert (value != null);
    value.value.add(new NamedValue(obj.getInfo(), SUB_ENTRY_NAME, uninit));

    return dataElem;
  }

  private void addVariables(State state, AstList<NamedElement> param, RecordType type) {
    RecordValue value = initValues.get(type);
    assert (value != null);

    for (StateVariable var : TypeFilter.select(state.item, StateVariable.class)) {
      NamedElement item = new NamedElement(var.getInfo(), getName(var), Copy.copy(var.type));
      type.element.add(item);
      value.value.add(new NamedValue(var.getInfo(), getName(var), Copy.copy(var.def)));

      AstList<NamedElement> path = new AstList<NamedElement>(param);
      path.add(item);
      epath.put(var, path);
    }

    ConstPrivate init = new ConstPrivate(state.getInfo(), CONST_PREFIX + getName(type), TypeRefFactory.create(state.getInfo(), type), value);
    initVar.put(type, init);
    state.item.add(init);
  }

}
