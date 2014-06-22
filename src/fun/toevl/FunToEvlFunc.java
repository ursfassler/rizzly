package fun.toevl;

import java.util.Map;

import evl.Evl;
import evl.function.FunctionBase;
import evl.function.impl.FuncIfaceInRet;
import evl.function.impl.FuncIfaceInVoid;
import evl.function.impl.FuncIfaceOutRet;
import evl.function.impl.FuncIfaceOutVoid;
import evl.function.impl.FuncPrivateVoid;
import evl.other.ListOfNamed;
import evl.statement.Block;
import evl.type.Type;
import evl.type.TypeRef;
import fun.Fun;
import fun.NullTraverser;
import fun.function.FunctionHeader;
import fun.function.impl.FuncEntryExit;
import fun.function.impl.FuncGlobal;
import fun.function.impl.FuncImplResponse;
import fun.function.impl.FuncImplSlot;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncProtQuery;
import fun.function.impl.FuncProtResponse;
import fun.function.impl.FuncProtSignal;
import fun.function.impl.FuncProtSlot;
import fun.variable.FuncVariable;

public class FunToEvlFunc extends NullTraverser<FunctionBase, Void> {
  private Map<Fun, Evl> map;
  private FunToEvl fta;

  public FunToEvlFunc(FunToEvl fta, Map<Fun, Evl> map) {
    super();
    this.map = map;
    this.fta = fta;
  }

  @Override
  protected FunctionBase visit(Fun obj, Void param) {
    FunctionBase cobj = (FunctionBase) map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected FunctionBase visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------

  public ListOfNamed<evl.variable.FuncVariable> genpa(FunctionHeader obj) {
    ListOfNamed<evl.variable.FuncVariable> fparam = new ListOfNamed<evl.variable.FuncVariable>();
    for (FuncVariable itr : obj.getParam()) {
      fparam.add((evl.variable.FuncVariable) fta.traverse(itr, null));
    }
    return fparam;
  }

  @Override
  protected FunctionBase visitFuncProtSlot(FuncProtSlot obj, Void param) {
    return new FuncIfaceInVoid(obj.getInfo(), obj.getName(), genpa(obj));
  }

  @Override
  protected FunctionBase visitFuncProtSignal(FuncProtSignal obj, Void param) {
    return new FuncIfaceOutVoid(obj.getInfo(), obj.getName(), genpa(obj));
  }

  @Override
  protected FunctionBase visitFuncProtQuery(FuncProtQuery obj, Void param) {
    FuncIfaceOutRet func = new FuncIfaceOutRet(obj.getInfo(), obj.getName(), genpa(obj));
    Type nt = (Type) fta.traverse(FunToEvl.getRefType(obj.getRet()), null);
    func.setRet(new TypeRef(obj.getRet().getInfo(), nt));
    return func;
  }

  @Override
  protected FunctionBase visitFuncProtResponse(FuncProtResponse obj, Void param) {
    FuncIfaceInRet func = new FuncIfaceInRet(obj.getInfo(), obj.getName(), genpa(obj));
    Type nt = (Type) fta.traverse(FunToEvl.getRefType(obj.getRet()), null);
    func.setRet(new TypeRef(obj.getRet().getInfo(), nt));
    return func;
  }

  @Override
  protected FunctionBase visitFuncEntryExit(FuncEntryExit obj, Void param) {
    FuncPrivateVoid func = new FuncPrivateVoid(obj.getInfo(), obj.getName(), genpa(obj));
    func.setBody((Block) fta.visit(obj.getBody(), null));
    return func;
  }

  @Override
  protected FunctionBase visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    evl.function.impl.FuncPrivateRet func = new evl.function.impl.FuncPrivateRet(obj.getInfo(), obj.getName(), genpa(obj));
    Type nt = (Type) fta.traverse(FunToEvl.getRefType(obj.getRet()), null);
    func.setRet(new TypeRef(obj.getRet().getInfo(), nt));
    func.setBody((Block) fta.visit(obj.getBody(), null));
    return func;
  }

  @Override
  protected FunctionBase visitFuncPrivateVoid(fun.function.impl.FuncPrivateVoid obj, Void param) {
    FuncPrivateVoid func = new FuncPrivateVoid(obj.getInfo(), obj.getName(), genpa(obj));
    func.setBody((Block) fta.visit(obj.getBody(), null));
    return func;
  }

  @Override
  protected FunctionBase visitFuncImplResponse(FuncImplResponse obj, Void param) {
    evl.function.impl.FuncImplResponse func = new evl.function.impl.FuncImplResponse(obj.getInfo(), obj.getName(), genpa(obj));
    Type nt = (Type) fta.traverse(FunToEvl.getRefType(obj.getRet()), null);
    func.setRet(new TypeRef(obj.getRet().getInfo(), nt));
    func.setBody((Block) fta.visit(obj.getBody(), null));
    return func;
  }

  @Override
  protected FunctionBase visitFuncImplSlot(FuncImplSlot obj, Void param) {
    evl.function.impl.FuncImplSlot func = new evl.function.impl.FuncImplSlot(obj.getInfo(), obj.getName(), genpa(obj));
    func.setBody((Block) fta.visit(obj.getBody(), null));
    return func;
  }

  @Override
  protected FunctionBase visitFuncGlobal(FuncGlobal obj, Void param) {
    evl.function.impl.FuncGlobal func = new evl.function.impl.FuncGlobal(obj.getInfo(), obj.getName(), genpa(obj));
    Type nt = (Type) fta.traverse(FunToEvl.getRefType(obj.getRet()), null);
    func.setRet(new TypeRef(obj.getRet().getInfo(), nt));
    func.setBody((Block) fta.visit(obj.getBody(), null));
    return func;
  }

  // @Override
  // protected FunctionBase visitFunctionHeader(fun.function.FunctionHeader obj, Void param) {
  // Class<? extends FunctionBase> kind = funType.get(obj);
  // if (kind == null) {
  // RError.err(ErrorType.Fatal, obj.getInfo(), "Unknown function type of function: " + obj + "[" +
  // obj.getClass().getCanonicalName() + "]");
  // }
  // FunctionBase func = FunctionFactory.create(kind, obj.getInfo(), obj.getName(), genpa(obj));
  //
  // assert ((func instanceof evl.function.FuncWithReturn) == (obj instanceof FuncWithReturn));
  // assert ((func instanceof evl.function.FuncWithBody) == (obj instanceof FuncWithBody));
  //
  // map.put(obj, func);
  // TypeRef retType;
  // if (obj instanceof FuncWithReturn) {
  // fun.type.Type ot = FunToEvl.getRefType(((FuncWithReturn) obj).getRet());
  // Type nt = (Type) fta.traverse(ot, null);
  // retType = new TypeRef(((FuncWithReturn) obj).getRet().getInfo(), (Type) nt);
  // ((evl.function.FuncWithReturn) func).setRet(new TypeRef(retType.getInfo(), retType.getRef()));
  // } else {
  // retType = new TypeRef(new ElementInfo(), new VoidType());
  // // FIXME get singleton
  // }
  // if (obj instanceof FuncWithBody) {
  // Block body = (Block) fta.visit(((FuncWithBody) obj).getBody(), null);
  // ((evl.function.FuncWithBody) func).setBody(body);
  // }
  // return func;
  // }

}
