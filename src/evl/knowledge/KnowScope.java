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

package evl.knowledge;

import java.util.HashSet;
import java.util.Set;

import common.Scope;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.component.composition.CompUse;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateSimple;
import evl.data.component.hfsm.Transition;
import evl.data.expression.reference.DummyLinkTarget;
import evl.data.file.RizzlyFile;
import evl.data.function.header.FuncFunction;
import evl.data.function.header.FuncProcedure;
import evl.data.function.header.FuncQuery;
import evl.data.function.header.FuncResponse;
import evl.data.function.header.FuncSignal;
import evl.data.function.header.FuncSlot;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumElement;
import evl.data.type.base.EnumType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.composed.RecordType;
import evl.data.type.composed.UnsafeUnionType;
import evl.data.type.special.AnyType;
import evl.data.type.special.IntegerType;
import evl.data.type.special.NaturalType;
import evl.data.type.special.VoidType;
import evl.data.type.template.ArrayTemplate;
import evl.data.type.template.RangeTemplate;
import evl.data.type.template.TypeType;
import evl.data.type.template.TypeTypeTemplate;
import evl.data.variable.ConstGlobal;
import evl.data.variable.ConstPrivate;
import evl.data.variable.FuncVariable;
import evl.data.variable.StateVariable;
import evl.data.variable.TemplateParameter;
import fun.other.RawComposition;
import fun.other.RawElementary;
import fun.other.RawHfsm;
import fun.other.Template;

public class KnowScope extends KnowledgeEntry {
  @SuppressWarnings("rawtypes")
  static final private Set<Class> global = new HashSet<Class>();
  @SuppressWarnings("rawtypes")
  static final private Set<Class> local = new HashSet<Class>();
  @SuppressWarnings("rawtypes")
  static final private Set<Class> priv = new HashSet<Class>();

  static {
    global.add(ConstGlobal.class);

    global.add(IntegerType.class);
    global.add(ArrayTemplate.class);
    global.add(RangeType.class);
    global.add(RangeTemplate.class);
    global.add(NaturalType.class);
    global.add(TypeTypeTemplate.class);
    global.add(AnyType.class);
    global.add(VoidType.class);
    global.add(RecordType.class);
    global.add(UnsafeUnionType.class);
    global.add(ArrayType.class);
    global.add(StringType.class);
    global.add(TypeType.class);
    global.add(EnumType.class);
    global.add(BooleanType.class);
    global.add(EnumElement.class);
    global.add(RawElementary.class);
    global.add(RawComposition.class);
    global.add(RawHfsm.class);
    global.add(DummyLinkTarget.class);
    global.add(RizzlyFile.class);
    global.add(FuncFunction.class); // FIXME: can also be private

    local.add(FuncVariable.class);
    local.add(TemplateParameter.class);

    priv.add(CompUse.class);
    priv.add(StateVariable.class);
    priv.add(ConstPrivate.class);
    priv.add(StateComposite.class);
    priv.add(StateSimple.class);
    priv.add(Transition.class);
    priv.add(FuncResponse.class); // TODO: sure?
    priv.add(FuncQuery.class); // TODO: sure?
    priv.add(FuncSignal.class); // TODO: sure?
    priv.add(FuncSlot.class); // TODO: sure?
    priv.add(FuncProcedure.class); // FIXME: can also be private

    {
      @SuppressWarnings("rawtypes")
      Set<Class> all = new HashSet<Class>();
      all.addAll(global);
      all.addAll(local);
      all.addAll(priv);
      assert (all.size() == (global.size() + local.size() + priv.size()));
    }
  }

  @Override
  public void init(evl.knowledge.KnowledgeBase base) {
  }

  static public Scope get(Evl obj) {
    if (global.contains(obj.getClass())) {
      return Scope.global;
    } else if (local.contains(obj.getClass())) {
      return Scope.local;
    } else if (priv.contains(obj.getClass())) {
      return Scope.privat;
    } else if (obj instanceof Template) {
      return get(((Template) obj).getObject());
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled class: " + obj.getClass().getCanonicalName());
      return null;
    }
  }

}
