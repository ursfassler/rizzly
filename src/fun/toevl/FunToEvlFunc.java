package fun.toevl;

import java.util.Map;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.function.FunctionFactory;
import evl.other.ListOfNamed;
import evl.statement.Block;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.special.VoidType;
import fun.Fun;
import fun.NullTraverser;
import fun.function.FuncWithBody;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
import fun.variable.FuncVariable;

public class FunToEvlFunc extends NullTraverser<FunctionBase, Void> {
  private Map<Fun, Evl> map;
  private Map<FunctionHeader, Class<? extends FunctionBase>> funType;
  private FunToEvl fta;

  public FunToEvlFunc(FunToEvl fta, Map<Fun, Evl> map, Map<FunctionHeader, Class<? extends FunctionBase>> funType) {
    super();
    this.map = map;
    this.fta = fta;
    this.funType = funType;
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
  protected FunctionBase visitFunctionHeader(fun.function.FunctionHeader obj, Void param) {
    Class<? extends FunctionBase> kind = funType.get(obj);
    if (kind == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unknown function type of function: " + obj + "[" + obj.getClass().getCanonicalName() + "]");
    }
    FunctionBase func = FunctionFactory.create(kind, obj.getInfo(), obj.getName(), genpa(obj));

    assert ((func instanceof evl.function.FuncWithReturn) == (obj instanceof FuncWithReturn));
    assert ((func instanceof evl.function.FuncWithBody) == (obj instanceof FuncWithBody));

    map.put(obj, func);
    TypeRef retType;
    if (obj instanceof FuncWithReturn) {
      Reference ref = (Reference) fta.traverse(((FuncWithReturn) obj).getRet(), null);
      assert (ref.getOffset().isEmpty());
      retType = new TypeRef(ref.getInfo(), (Type) ref.getLink());
      ((evl.function.FuncWithReturn) func).setRet(new TypeRef(retType.getInfo(), retType.getRef()));
    } else {
      retType = new TypeRef(new ElementInfo(), new VoidType()); // FIXME get singleton
    }
    if (obj instanceof FuncWithBody) {
      Block body = (Block) fta.visit(((FuncWithBody) obj).getBody(), null);
      ((evl.function.FuncWithBody) func).setBody(body);
    }
    return func;
  }

}
