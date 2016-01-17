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

package ast.knowledge;

import java.util.HashSet;
import java.util.Set;

import ast.data.Ast;
import ast.data.component.composition.CompUse;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.file.RizzlyFile;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.raw.RawComposition;
import ast.data.raw.RawElementary;
import ast.data.raw.RawHfsm;
import ast.data.reference.LinkTarget;
import ast.data.template.Template;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.type.special.AnyType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.data.type.special.TypeType;
import ast.data.type.special.VoidType;
import ast.data.type.template.ArrayTemplate;
import ast.data.type.template.RangeTemplate;
import ast.data.type.template.TypeTypeTemplate;
import ast.data.variable.ConstGlobal;
import ast.data.variable.ConstPrivate;
import ast.data.variable.FunctionVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;
import error.ErrorType;
import error.RError;

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
    global.add(LinkTarget.class);
    global.add(RizzlyFile.class);
    global.add(FuncFunction.class); // FIXME: can also be private

    local.add(FunctionVariable.class);
    local.add(TemplateParameter.class);

    priv.add(CompUse.class);
    priv.add(StateVariable.class);
    priv.add(ConstPrivate.class);
    priv.add(StateComposite.class);
    priv.add(StateSimple.class);
    priv.add(Transition.class);
    priv.add(FuncResponse.class); // TODO: sure?
    priv.add(FuncQuery.class); // TODO: sure?
    priv.add(Signal.class); // TODO: sure?
    priv.add(Slot.class); // TODO: sure?
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
  public void init(ast.knowledge.KnowledgeBase base) {
  }

  static public Scope get(Ast obj) {
    if (global.contains(obj.getClass())) {
      return Scope.global;
    } else if (local.contains(obj.getClass())) {
      return Scope.local;
    } else if (priv.contains(obj.getClass())) {
      return Scope.privat;
    } else if (obj instanceof Template) {
      return get(((Template) obj).getObject());
    } else {
      RError.err(ErrorType.Fatal, "Unhandled class: " + obj.getClass().getCanonicalName(), obj.metadata());
      return null;
    }
  }

}
