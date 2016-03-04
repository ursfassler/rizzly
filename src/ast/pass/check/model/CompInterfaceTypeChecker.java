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

package ast.pass.check.model;

import java.util.List;

import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.Component;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.Connection;
import ast.data.component.composition.Direction;
import ast.data.component.composition.Endpoint;
import ast.data.component.composition.EndpointSelf;
import ast.data.component.composition.EndpointSub;
import ast.data.component.composition.ImplComposition;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.Transition;
import ast.data.function.Function;
import ast.data.function.InterfaceFunction;
import ast.data.function.header.FuncQuery;
import ast.data.function.header.Response;
import ast.data.function.header.Signal;
import ast.data.function.header.Slot;
import ast.data.type.Type;
import ast.data.variable.Constant;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowLeftIsContainerOfRight;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;
import ast.pass.AstPass;
import ast.pass.check.model.composition.QueryIsConnectedToOneResponse;
import ast.repository.query.Collector;
import ast.repository.query.EndpointFunctionQuery;
import ast.repository.query.Referencees.TargetResolver;
import ast.specification.IsClass;
import ast.visitor.VisitExecutorImplementation;
import error.ErrorType;
import error.RError;

public class CompInterfaceTypeChecker implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TargetResolver targetResolver = new TargetResolver();
    CompInterfaceTypeCheckerWorker adder = new CompInterfaceTypeCheckerWorker(targetResolver, kb);
    adder.traverse(ast, null);
  }

}

class CompInterfaceTypeCheckerWorker extends NullDispatcher<Void, Void> {
  final private TargetResolver targetResolver;
  final private KnowType kt;
  final private KnowLeftIsContainerOfRight kc;

  public CompInterfaceTypeCheckerWorker(TargetResolver targetResolver, KnowledgeBase kb) {
    this.targetResolver = targetResolver;
    this.kt = kb.getEntry(KnowType.class);
    this.kc = kb.getEntry(KnowLeftIsContainerOfRight.class);
  }

  @Override
  protected Void visitDefault(Ast obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  @Override
  protected Void visitCompUse(ComponentUse obj, Void param) {
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Void param) {
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitList(obj.children, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    // TODO can we merge with test for elementary?
    // TODO test also other stuff?
    List<Transition> transList = Collector.select(obj, new IsClass(Transition.class)).castTo(Transition.class);

    for (Transition tr : transList) {
      // TODO check if tr.getEventFunc() has compatible parameters
    }

    return null; // TODO check if all queries are defined
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Void param) {
    assert (obj.component.isEmpty());
    assert (obj.subCallback.isEmpty());
    return null;
  }

  @Override
  protected Void visitType(Type obj, Void param) {
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    // TODO do checks over whole implementation, i.e. not splitting when
    // functions has return value
    // TODO check for cycles
    visitList(obj.connection, param);

    checkSelfIface(obj, Direction.out);
    checkSelfIface(obj, Direction.in);

    for (ComponentUse use : obj.component) {
      checkIface(obj, use, Direction.in);
      checkIface(obj, use, Direction.out);
    }

    // TODO move into own pass or in a common pass
    (new QueryIsConnectedToOneResponse(targetResolver, RError.instance())).check(obj.connection);

    return null;
  }

  private void checkSelfIface(ImplComposition obj, Direction dir) {
    for (InterfaceFunction ifaceuse : obj.getIface(dir)) {
      if (!ifaceIsConnected(ifaceuse, dir, obj.connection)) {
        ErrorType etype;
        if (ifaceuse instanceof Response) {
          etype = ErrorType.Error;
        } else {
          etype = ErrorType.Hint;
        }
        RError.err(etype, "Interface " + ifaceuse.getName() + " not connected", ifaceuse.metadata());
      }
    }
  }

  private Component checkIface(ImplComposition obj, ComponentUse use, Direction dir) {
    Component type = targetResolver.targetOf(use.getCompRef(), Component.class);
    for (InterfaceFunction ifaceuse : type.getIface(dir)) {
      if (!ifaceIsConnected(use, ifaceuse, dir.other(), obj.connection)) {
        ErrorType etype;
        if (ifaceuse instanceof FuncQuery) {
          etype = ErrorType.Error;
        } else {
          etype = ErrorType.Hint;
        }
        RError.err(ErrorType.Hint, "Interface " + ifaceuse.getName() + " declared here", ifaceuse.metadata());
        RError.err(etype, "Interface " + use.getName() + "." + ifaceuse.getName() + " not connected", use.metadata());
      }
    }
    return type;
  }

  private boolean ifaceIsConnected(ComponentUse use, InterfaceFunction ifaceuse, Direction dir, List<Connection> connection) {
    for (Connection itr : connection) {
      if (getEndpoint(itr, dir) instanceof EndpointSub) {
        EndpointSub ep = (EndpointSub) getEndpoint(itr, dir);
        EndpointFunctionQuery query = new EndpointFunctionQuery(targetResolver);
        VisitExecutorImplementation.instance().visit(query, ep);
        Function func = query.getFunction();
        if ((targetResolver.targetOf(ep.getComponent(), ComponentUse.class) == use) && (func == ifaceuse)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean ifaceIsConnected(InterfaceFunction ifaceuse, Direction dir, List<Connection> connection) {
    for (Connection itr : connection) {
      if (getEndpoint(itr, dir) instanceof EndpointSelf) {
        Endpoint ep = getEndpoint(itr, dir);
        EndpointFunctionQuery query = new EndpointFunctionQuery(targetResolver);
        VisitExecutorImplementation.instance().visit(query, ep);
        Function func = query.getFunction();
        if (func == ifaceuse) {
          return true;
        }
      }
    }
    return false;
  }

  private Endpoint getEndpoint(Connection connection, Direction dir) {
    if (dir == Direction.in) {
      return connection.getSrc();
    } else {
      return connection.getDst();
    }
  }

  @Override
  protected Void visitConnection(Connection obj, Void param) {
    Endpoint srcEp = obj.getSrc();
    Endpoint dstEp = obj.getDst();
    Function srcType = getIfaceFunc(srcEp);
    Function dstType = getIfaceFunc(dstEp);

    Type st = kt.get(srcType);
    Type dt = kt.get(dstType);

    if (!kc.get(dt, st)) {
      RError.err(ErrorType.Error, "Invalid connection: " + st + " -> " + dt, obj.metadata());
    }

    boolean srcSelf = srcEp instanceof EndpointSelf;
    boolean dstSelf = dstEp instanceof EndpointSelf;
    Direction srcDir = getDir(srcType);
    Direction dstDir = getDir(dstType);

    if (srcSelf && dstSelf) {
      checkDir(srcDir, Direction.in, Direction.in, srcEp.metadata());
      checkDir(dstDir, Direction.out, Direction.out, dstEp.metadata());
    } else if (!srcSelf && dstSelf) {
      checkDir(srcDir, Direction.out, Direction.in, srcEp.metadata());
      checkDir(dstDir, Direction.out, Direction.out, dstEp.metadata());
    } else if (srcSelf && !dstSelf) {
      checkDir(srcDir, Direction.in, Direction.in, srcEp.metadata());
      checkDir(dstDir, Direction.in, Direction.out, dstEp.metadata());
    } else {
      assert (!srcSelf && !dstSelf);
      checkDir(srcDir, Direction.out, Direction.in, srcEp.metadata());
      checkDir(dstDir, Direction.in, Direction.out, dstEp.metadata());
    }

    return null;
  }

  private Function getIfaceFunc(Endpoint ep) {
    EndpointFunctionQuery query = new EndpointFunctionQuery(targetResolver);
    VisitExecutorImplementation.instance().visit(query, ep);
    Function func = query.getFunction();
    if (func == null) {
      RError.err(ErrorType.Error, "Interface not found: " + ep.toString(), ep.metadata());
    }
    return func;
  }

  private Direction getDir(Function func) {
    if (func instanceof Slot) {
      return Direction.in;
    } else if (func instanceof Response) {
      return Direction.in;
    } else if (func instanceof FuncQuery) {
      return Direction.out;
    } else if (func instanceof Signal) {
      return Direction.out;
    } else {
      RError.err(ErrorType.Fatal, "Unexpected function type: " + func.getClass().getCanonicalName(), func.metadata());
      return null;
    }
  }

  private void checkDir(Direction is, Direction should, Direction ep, MetaList info) {
    if (is != should) {
      String eps = ep == Direction.in ? "from" : "to";
      String iss = is == Direction.in ? "input" : "output";
      RError.err(ErrorType.Error, "can not connect " + eps + " " + iss, info);
    }
  }

}
