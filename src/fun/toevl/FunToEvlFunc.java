package fun.toevl;

import java.util.Map;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.cfg.BasicBlockList;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.function.FunctionFactory;
import evl.other.ListOfNamed;
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

  public ListOfNamed<evl.variable.Variable> genpa(FunctionHeader obj) {
    ListOfNamed<evl.variable.Variable> fparam = new ListOfNamed<evl.variable.Variable>();
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
    if (obj instanceof FuncWithReturn) {
      ((evl.function.FuncWithReturn) func).setRet((Reference) fta.traverse(((FuncWithReturn) obj).getRet(), null));
    }
    if (obj instanceof FuncWithBody) {
      MakeBasicBlocks blocks = new MakeBasicBlocks(fta);
      BasicBlockList nbody = blocks.translate(((FuncWithBody) obj).getBody(), obj.getParam().getList());
      ((evl.function.FuncWithBody) func).setBody(nbody);
    }
    return func;
  }

}
