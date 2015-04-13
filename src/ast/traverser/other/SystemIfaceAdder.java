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

package ast.traverser.other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import pass.AstPass;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.CompUse;
import ast.data.component.composition.ImplComposition;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.expression.Expression;
import ast.data.expression.TupleValue;
import ast.data.expression.reference.RefCall;
import ast.data.expression.reference.RefName;
import ast.data.expression.reference.Reference;
import ast.data.function.Function;
import ast.data.function.header.FuncSlot;
import ast.data.function.ret.FuncReturnNone;
import ast.data.statement.Block;
import ast.data.statement.CallStmt;
import ast.data.statement.Statement;
import ast.data.variable.FuncVariable;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.traverser.NullTraverser;

import common.Designator;
import common.ElementInfo;

import error.RError;

public class SystemIfaceAdder extends AstPass {
  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    SystemIfaceAdderWorker reduction = new SystemIfaceAdderWorker();
    reduction.traverse(ast, null);
    SystemIfaceCaller caller = new SystemIfaceCaller(reduction.getCtors(), reduction.getDtors());
    caller.traverse(ast, null);

    kb.clear(KnowType.class);
  }
}

class SystemIfaceAdderWorker extends NullTraverser<Void, Void> {
  public static final String DESTRUCT = Designator.NAME_SEP + "destruct";
  public static final String CONSTRUCT = Designator.NAME_SEP + "construct";
  final private HashMap<Component, Function> ctors = new HashMap<Component, Function>();
  final private HashMap<Component, Function> dtors = new HashMap<Component, Function>();

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    return null;
    // throw new RuntimeException("not yet implemented: " +
    // obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.children, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    throw new RuntimeException("Forgot hfsm reduction phase, dude");
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("Forgot composition reduction phase, dude");
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    Function ctor = makeFunc(obj, CONSTRUCT);
    getCtors().put(obj, ctor);

    Function dtor = makeFunc(obj, DESTRUCT);
    getDtors().put(obj, dtor);

    return null;
  }

  private Function makeFunc(ImplElementary obj, String name) {
    ElementInfo info = ElementInfo.NO;
    FuncSlot rfunc = new FuncSlot(info, name, new AstList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
    obj.iface.add(rfunc);
    return rfunc;
  }

  public HashMap<Component, Function> getCtors() {
    return ctors;
  }

  public HashMap<Component, Function> getDtors() {
    return dtors;
  }
}

class SystemIfaceCaller extends NullTraverser<Void, Void> {
  final private HashMap<Component, Function> ctors;
  final private HashMap<Component, Function> dtors;

  public SystemIfaceCaller(HashMap<Component, Function> ctors, HashMap<Component, Function> dtors) {
    super();
    this.ctors = ctors;
    this.dtors = dtors;
  }

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.children, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    throw new RuntimeException("Forgot hfsm reduction phase, dude");
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    throw new RuntimeException("Forgot composition reduction phase, dude");
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    AstList<CompUse> compList = new AstList<CompUse>(obj.component);
    // FIXME this order may cause errors as it is not granted to be topological
    // order

    Function ctor = getCtor(obj);
    Function dtor = getDtor(obj);

    {
      ArrayList<Statement> code = new ArrayList<Statement>();
      for (CompUse cuse : compList) {
        Function sctor = getCtor((Component) cuse.compRef.getTarget());
        CallStmt call = makeCall(cuse, sctor);
        code.add(call);
      }
      code.add(makeCall(obj.entryFunc.link));

      ctor.body.statements.addAll(code);
    }

    {
      ArrayList<Statement> code = new ArrayList<Statement>();
      code.add(makeCall(obj.exitFunc.link));
      Collections.reverse(compList);
      for (CompUse cuse : compList) {
        Function sdtor = getDtor((Component) cuse.compRef.getTarget());
        CallStmt call = makeCall(cuse, sdtor);
        code.add(call);
      }

      dtor.body.statements.addAll(code);
    }

    return null;
  }

  private Function getDtor(Component obj) {
    Function dtor = dtors.get(obj);
    RError.ass(dtor != null, obj.getInfo(), "dtor is null");
    return dtor;
  }

  private Function getCtor(Component obj) {
    Function ctor = ctors.get(obj);
    RError.ass(ctor != null, obj.getInfo(), "ctor is null");
    return ctor;
  }

  private CallStmt makeCall(Function ref) {
    ElementInfo info = ElementInfo.NO;
    assert (ref.param.isEmpty());
    Reference call = new Reference(ref.getInfo(), ref);
    call.offset.add(new RefCall(info, new TupleValue(info, new AstList<Expression>())));
    return new CallStmt(info, call);
  }

  @Deprecated
  private CallStmt makeCall(CompUse self, Function func) {
    ElementInfo info = ElementInfo.NO;
    RError.ass(func.param.isEmpty(), func.getInfo(), "expected (de)constructor to have no parameter");
    Reference fref = new Reference(info, self);
    fref.offset.add(new RefName(info, func.name));
    fref.offset.add(new RefCall(info, new TupleValue(info, new AstList<Expression>())));
    return new CallStmt(info, fref);
  }
}