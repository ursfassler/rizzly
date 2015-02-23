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

package evl.traverser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import pass.EvlPass;

import common.Designator;
import common.ElementInfo;

import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.expression.Expression;
import evl.expression.TupleValue;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.function.Function;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.ret.FuncReturnNone;
import evl.hfsm.ImplHfsm;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.EvlList;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.variable.FuncVariable;

public class SystemIfaceAdder extends EvlPass {
  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    SystemIfaceAdderWorker reduction = new SystemIfaceAdderWorker(kb);
    reduction.traverse(evl, null);
    SystemIfaceCaller caller = new SystemIfaceCaller(reduction.getCtors(), reduction.getDtors());
    caller.traverse(evl, null);

    kb.clear(KnowType.class);
  }
}

class SystemIfaceAdderWorker extends NullTraverser<Void, Void> {
  public static final String DESTRUCT = Designator.NAME_SEP + "destruct";
  public static final String CONSTRUCT = Designator.NAME_SEP + "construct";
  final private KnowledgeBase kb;
  final private HashMap<Component, Function> ctors = new HashMap<Component, Function>();
  final private HashMap<Component, Function> dtors = new HashMap<Component, Function>();

  public SystemIfaceAdderWorker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    return null;
    // throw new RuntimeException("not yet implemented: " +
    // obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.getChildren(), param);
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
    FuncCtrlInDataIn rfunc = new FuncCtrlInDataIn(info, name, new EvlList<FuncVariable>(), new FuncReturnNone(info), new Block(info));
    obj.getIface().add(rfunc);
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
  protected Void visitDefault(Evl obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.getChildren(), param);
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
    EvlList<CompUse> compList = new EvlList<CompUse>(obj.getComponent());
    // FIXME this order may cause errors as it is not granted to be topological
    // order

    Function ctor = getCtor(obj);
    Function dtor = getDtor(obj);

    {
      ArrayList<Statement> code = new ArrayList<Statement>();
      for (CompUse cuse : compList) {
        Function sctor = getCtor(cuse.getLink());
        CallStmt call = makeCall(cuse, sctor);
        code.add(call);
      }
      code.add(makeCall(obj.getEntryFunc().getLink()));

      ctor.getBody().getStatements().addAll(code);
    }

    {
      ArrayList<Statement> code = new ArrayList<Statement>();
      code.add(makeCall(obj.getExitFunc().getLink()));
      Collections.reverse(compList);
      for (CompUse cuse : compList) {
        Function sdtor = getDtor(cuse.getLink());
        CallStmt call = makeCall(cuse, sdtor);
        code.add(call);
      }

      dtor.getBody().getStatements().addAll(code);
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
    assert (ref.getParam().isEmpty());
    Reference call = new Reference(ref.getInfo(), ref);
    call.getOffset().add(new RefCall(info, new TupleValue(info, new EvlList<Expression>())));
    return new CallStmt(info, call);
  }

  @Deprecated
  private CallStmt makeCall(CompUse self, Function func) {
    ElementInfo info = ElementInfo.NO;
    RError.ass(func.getParam().isEmpty(), func.getInfo(), "expected (de)constructor to have no parameter");
    Reference fref = new Reference(info, self);
    fref.getOffset().add(new RefName(info, func.getName()));
    fref.getOffset().add(new RefCall(info, new TupleValue(info, new EvlList<Expression>())));
    return new CallStmt(info, fref);
  }
}
