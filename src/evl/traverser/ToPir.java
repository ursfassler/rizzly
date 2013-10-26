package evl.traverser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pir.PirObject;
import pir.expression.PExpression;
import pir.expression.reference.RefItem;
import pir.expression.reference.VarRef;
import pir.expression.reference.VarRefConst;
import pir.expression.reference.VarRefSimple;
import pir.expression.reference.VarRefStatevar;
import pir.function.FuncImpl;
import pir.function.FuncProto;
import pir.function.Function;
import pir.know.KnowBaseItem;
import pir.know.KnowledgeBase;
import pir.other.PirValue;
import pir.other.Program;
import pir.other.Variable;
import pir.statement.bbend.BasicBlockEnd;
import pir.statement.normal.CallAssignment;
import pir.statement.normal.StoreStmt;
import pir.statement.normal.VariableGeneratorStmt;
import pir.type.NamedElement;
import pir.type.StructType;
import pir.type.TypeRef;
import pir.type.UnionSelector;
import util.NumberSet;
import util.Range;
import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.StringValue;
import evl.expression.binop.And;
import evl.expression.binop.Div;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.RefPtrDeref;
import evl.expression.reference.Reference;
import evl.expression.unop.Not;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.FunctionHeader;
import evl.other.RizzlyProgram;
import evl.statement.bbend.CaseGoto;
import evl.statement.bbend.CaseGotoOpt;
import evl.statement.bbend.CaseOptRange;
import evl.statement.bbend.CaseOptValue;
import evl.statement.bbend.Goto;
import evl.statement.bbend.IfGoto;
import evl.statement.bbend.ReturnExpr;
import evl.statement.bbend.ReturnVoid;
import evl.statement.bbend.Unreachable;
import evl.statement.normal.Assignment;
import evl.statement.normal.CallStmt;
import evl.statement.normal.GetElementPtr;
import evl.statement.normal.LoadStmt;
import evl.statement.normal.NormalStmt;
import evl.statement.normal.StackMemoryAlloc;
import evl.statement.normal.TypeCast;
import evl.statement.normal.VarDefInitStmt;
import evl.statement.normal.VarDefStmt;
import evl.statement.phi.PhiStmt;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.IntegerType;
import evl.type.special.PointerType;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;

public class ToPir extends NullTraverser<PirObject, Void> {

  private Map<Evl, PirObject> map = new HashMap<Evl, PirObject>();
  KnowBaseItem kbi;
  private String rootdir;

  static public PirObject process(Evl ast, String rootdir) {
    ToPir toC = new ToPir(rootdir);
    return toC.traverse(ast, null);
  }

  public ToPir(String rootdir) {
    super();
    this.rootdir = rootdir;
  }

  @Override
  protected PirObject visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected PirObject visit(Evl obj, Void param) {
    PirObject cobj = map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected PirObject visitReference(Reference obj, Void param) {
    PirObject ref = visit(obj.getLink(), null);
    if (ref instanceof Variable) {
      if ((ref instanceof pir.other.SsaVariable) && (obj.getOffset().isEmpty())) {
        return new VarRefSimple((pir.other.SsaVariable) ref);
      } else {
        ArrayList<RefItem> offset = new ArrayList<RefItem>();
        for (evl.expression.reference.RefItem itm : obj.getOffset()) {
          offset.add((RefItem) visit(itm, null));
        }
        return new VarRef((Variable) ref, offset);
      }
    } else if (ref instanceof pir.type.Type) {
      assert (obj.getOffset().isEmpty());
      return new TypeRef((pir.type.Type) ref);
    } else if (ref instanceof Function) {
      assert ((obj.getOffset().size() == 1) && (obj.getOffset().get(0) instanceof RefCall));
      RefCall call = (RefCall) obj.getOffset().get(0);

      ArrayList<PirValue> acpa = new ArrayList<PirValue>();
      for (Expression pa : call.getActualParameter()) {
        acpa.add((PirValue) visit(pa, null));
      }

      return new pir.statement.normal.CallStmt((Function) ref, acpa); // TODO correct? return value not used?
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled class: " + ref.getClass().getCanonicalName());
    }

    return null;

    // PirObject ref = visit(obj.getLink(), null);
    // RefHead head = new RefHead((Referencable) ref);
    //
    // RefItem last = head;
    // for (evl.expression.reference.RefItem itr : obj.getOffset()) {
    // last = (RefItem) visit(itr, last);
    // }
    // return new pir.expression.reference.Reference(last);
  }

  @Override
  protected PirObject visitRefName(RefName obj, Void param) {
    return new pir.expression.reference.RefName(obj.getName());
  }

  @Override
  protected PirObject visitRefIndex(RefIndex obj, Void param) {
    PExpression index = (PExpression) visit(obj.getIndex(), null);
    return new pir.expression.reference.RefIndex(index);
  }

  @Override
  protected Program visitRizzlyProgram(RizzlyProgram obj, Void param) {
    Program prog = new Program(obj.getName());

    kbi = (new KnowledgeBase(prog, rootdir)).getEntry(KnowBaseItem.class);
    for (Type type : obj.getType()) {
      pir.type.Type ct = (pir.type.Type) visit(type, param);
      if (!prog.getType().contains(ct)) { // TODO why?
        prog.getType().add(ct);
      }
    }
    for (StateVariable itr : obj.getVariable()) {
      pir.other.StateVariable ct = (pir.other.StateVariable) visit(itr, param);
      prog.getVariable().add(ct);
    }
    for (Constant itr : obj.getConstant()) {
      pir.other.Constant ct = (pir.other.Constant) visit(itr, param);
      prog.getConstant().add(ct);
    }
    for (FunctionBase itr : obj.getFunction()) {
      pir.function.Function ct = (pir.function.Function) visit(itr, param);
      prog.getFunction().add(ct);
    }
    return prog;
  }

  @Override
  protected PirObject visitFunctionBase(FunctionBase obj, Void param) {
    String name = obj.getName();

    List<pir.other.SsaVariable> arg = new ArrayList<pir.other.SsaVariable>();
    for (evl.variable.Variable var : obj.getParam()) {
      assert (var instanceof SsaVariable);
      arg.add((pir.other.SsaVariable) visit(var, param));
    }

    pir.function.Function func;
    TypeRef retType;
    if (obj instanceof FuncWithReturn) {
      retType = (TypeRef) visit(((FuncWithReturn) obj).getRet(), null);
    } else {
      retType = new TypeRef(kbi.getVoidType());
    }

    if (obj instanceof FuncWithBody) {
      func = new FuncImpl(name, arg, retType);
    } else {
      func = new FuncProto(name, arg, retType);
    }
    func.getAttributes().addAll(obj.getAttributes());

    map.put(obj, func); // otherwise the compiler follows recursive calls

    if (obj instanceof FuncWithBody) {
      pir.cfg.BasicBlockList stmt = (pir.cfg.BasicBlockList) visit(((FuncWithBody) obj).getBody(), param);
      ((pir.function.FuncWithBody) func).setBody(stmt);
    }

    return func;
  }

  @Override
  protected pir.type.StructType visitRecordType(RecordType obj, Void param) {
    pir.type.StructType struct = new pir.type.StructType(obj.getName());
    for (evl.type.composed.NamedElement elem : obj.getElement()) {
      TypeRef type = (TypeRef) visit(elem.getType(), param);
      NamedElement celem = new NamedElement(elem.getName(), type);
      struct.getElements().add(celem);
    }
    return struct;
  }

  @Override
  protected PirObject visitUnionSelector(evl.type.composed.UnionSelector obj, Void param) {
    return new UnionSelector(obj.getName());
  }

  @Override
  protected PirObject visitUnionType(UnionType obj, Void param) {
    UnionSelector selector = (UnionSelector) visit(obj.getSelector(), null);
    pir.type.UnionType union = new pir.type.UnionType(obj.getName(), selector);
    for (evl.type.composed.NamedElement elem : obj.getElement()) {
      TypeRef type = (TypeRef) visit(elem.getType(), param);
      NamedElement celem = new NamedElement(elem.getName(), type);
      union.getElements().add(celem);
    }
    return union;
  }

  @Override
  protected PirObject visitEnumType(EnumType obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "EnumType should no longer exists; replaced with Range");
    return null;
  }

  @Override
  protected PirObject visitEnumElement(EnumElement obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "EnumElement should no longer exists");
    return null;
  }

  @Override
  protected PirObject visitVarDef(VarDefStmt obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "VarDefStmt should no longer exists; replaced with StackMemoryAlloc");
    return null;
  }

  @Override
  protected PirObject visitStackMemoryAlloc(StackMemoryAlloc obj, Void param) {
    pir.other.SsaVariable var = (pir.other.SsaVariable) visit(obj.getVariable(), null);
    pir.statement.normal.StackMemoryAlloc sma = new pir.statement.normal.StackMemoryAlloc(var);
    return sma;
  }

  @Override
  protected pir.statement.normal.CallStmt visitCallStmt(CallStmt obj, Void param) {
    PirObject call = visit(obj.getCall(), param);
    return (pir.statement.normal.CallStmt) call;
    // assert (call instanceof CallAssignment);
    // CallAssignment ass = (CallAssignment) call;
    // return new pir.statement.CallStmt(ass.getRef(), ass.getParameter()); // FIXME is that ok? just forget the defined
    // // variable?
  }

  @Override
  protected PirObject visitBasicBlockList(BasicBlockList obj, Void param) {
    pir.cfg.BasicBlockList ret = new pir.cfg.BasicBlockList((pir.cfg.BasicBlock) visit(obj.getEntry(), null), (pir.cfg.BasicBlock) visit(obj.getExit(), null));
    for (BasicBlock bb : obj.getBasicBlocks()) {
      ret.getBasicBlocks().add((pir.cfg.BasicBlock) visit(bb, param));
    }
    return ret;
  }

  @Override
  protected PirObject visitBoolValue(BoolValue obj, Void param) {
    pir.type.BooleanType type = kbi.getBooleanType();
    return new pir.expression.BoolValue(obj.isValue(), new TypeRef(type));
  }

  @Override
  protected pir.expression.Number visitNumber(Number obj, Void param) {
    pir.type.RangeType type = kbi.getRangeType(obj.getValue(), obj.getValue());
    return new pir.expression.Number(obj.getValue(), new TypeRef(type));
  }

  @Override
  protected PirObject visitStringValue(StringValue obj, Void param) {
    return new pir.expression.StringValue(obj.getValue());
  }

  @Override
  protected PirObject visitArrayValue(ArrayValue obj, Void param) {
    pir.expression.ArrayValue ret = new pir.expression.ArrayValue();
    for (Expression itr : obj.getValue()) {
      PirObject arg = visit(itr, param);
      assert (arg instanceof PExpression);
      ret.getValue().add((PExpression) arg);
    }
    return ret;
  }

  @Override
  protected PirObject visitPointerType(PointerType obj, Void param) {
    return new pir.type.PointerType((TypeRef) traverse(obj.getType(), null));
  }

  @Override
  protected PirObject visitNumSet(RangeType obj, Void param) {
    pir.type.RangeType ret = kbi.getRangeType(obj.getNumbers().getLow(), obj.getNumbers().getHigh());
    return ret;
  }

  @Override
  protected PirObject visitArrayType(ArrayType obj, Void param) {
    TypeRef elemType = (TypeRef) visit(obj.getType(), param);
    pir.type.ArrayType ret = new pir.type.ArrayType(obj.getSize(), elemType);
    return ret;
  }

  @Override
  protected pir.type.Type visitBooleanType(BooleanType obj, Void param) {
    pir.type.BooleanType ret = kbi.getBooleanType();
    return ret;
  }

  @Override
  protected PirObject visitReturnExpr(ReturnExpr obj, Void param) {
    PirObject expr = visit(obj.getExpr(), param);
    assert (expr instanceof PirValue);
    return new pir.statement.bbend.ReturnExpr((PirValue) expr);
  }

  @Override
  protected PirObject visitReturnVoid(ReturnVoid obj, Void param) {
    return new pir.statement.bbend.ReturnVoid();
  }

  @Override
  protected PirObject visitUnreachable(Unreachable obj, Void param) {
    return new pir.statement.bbend.Unreachable();
  }

  @Override
  protected PirObject visitVoidType(VoidType obj, Void param) {
    pir.type.VoidType ret = kbi.getVoidType();
    return ret;
  }

  @Override
  protected PirObject visitStringType(StringType obj, Void param) {
    pir.type.StringType ret = kbi.getStringType();
    return ret;
  }

  @Override
  protected PirObject visitFuncVariable(FuncVariable obj, Void param) {
    return new pir.other.FuncVariable(obj.getName(), (TypeRef) visit(obj.getType(), null));
  }

  @Override
  protected PirObject visitStateVariable(StateVariable obj, Void param) {
    return new pir.other.StateVariable(obj.getName(), (TypeRef) visit(obj.getType(), null));
  }

  @Override
  protected PirObject visitConstant(Constant obj, Void param) {
    pir.type.TypeRef type = (pir.type.TypeRef) visit(obj.getType(), null);
    PExpression def = (PExpression) visit(obj.getDef(), null);
    return new pir.other.Constant(obj.getName(), type, def);
  }

  @Override
  protected PirObject visitPhiStmt(PhiStmt obj, Void param) {
    pir.statement.phi.PhiStmt ret = new pir.statement.phi.PhiStmt((pir.other.SsaVariable) visit(obj.getVariable(), null));
    for (BasicBlock in : obj.getInBB()) {
      pir.other.PirValue var = (pir.other.PirValue) visit(obj.getArg(in), null);
      ret.addArg((pir.cfg.BasicBlock) visit(in, null), var);
    }
    return ret;
  }

  @Override
  protected PirObject visitBasicBlock(BasicBlock obj, Void param) {
    pir.cfg.BasicBlock ret = new pir.cfg.BasicBlock(obj.getName());
    map.put(obj, ret);

    for (PhiStmt phi : obj.getPhi()) {
      ret.getPhi().add((pir.statement.phi.PhiStmt) visit(phi, null));
    }
    for (NormalStmt stmt : obj.getCode()) {
      ret.getCode().add((pir.statement.normal.NormalStmt) visit(stmt, null));
    }
    ret.setEnd((BasicBlockEnd) visit(obj.getEnd(), null));

    return ret;
  }

  @Override
  protected PirObject visitCaseGoto(CaseGoto obj, Void param) {
    PirObject cond = visit(obj.getCondition(), null);
    assert (cond instanceof VarRefSimple);
    pir.other.SsaVariable var = ((VarRefSimple) cond).getRef();
    pir.statement.bbend.CaseGoto caseGoto = new pir.statement.bbend.CaseGoto(new VarRefSimple(var), (pir.cfg.BasicBlock) visit(obj.getOtherwise(), null));
    for (CaseGotoOpt opt : obj.getOption()) {
      pir.statement.bbend.CaseGotoOpt nopt = (pir.statement.bbend.CaseGotoOpt) visit(opt, null);
      caseGoto.getOption().add(nopt);
    }
    return caseGoto;
  }

  @Override
  protected PirObject visitCaseGotoOpt(CaseGotoOpt obj, Void param) {
    List<Range> list = new ArrayList<Range>();
    for (evl.statement.bbend.CaseOptEntry entry : obj.getValue()) {
      BigInteger low, high;
      if (entry instanceof CaseOptRange) {
        low = ((Number) ((CaseOptRange) entry).getStart()).getValue();
        high = ((Number) ((CaseOptRange) entry).getEnd()).getValue();
      } else if (entry instanceof CaseOptValue) {
        low = ((Number) ((CaseOptValue) entry).getValue()).getValue();
        high = low;
      } else {
        RError.err(ErrorType.Fatal, obj.getInfo(), "not yet implemented " + entry.getClass().getCanonicalName());
        low = null;
        high = null;
      }
      Range range = new Range(low, high);
      list.add(range);
    }
    return new pir.statement.bbend.CaseGotoOpt(new NumberSet(list), (pir.cfg.BasicBlock) visit(obj.getDst(), null));
  }

  @Override
  protected PirObject visitIfGoto(IfGoto obj, Void param) {
    PirValue cond = (PirValue) visit(obj.getCondition(), null);
    assert (cond.getType().getRef() instanceof pir.type.BooleanType);
    return new pir.statement.bbend.IfGoto(cond, (pir.cfg.BasicBlock) visit(obj.getThenBlock(), null), (pir.cfg.BasicBlock) visit(obj.getElseBlock(), null));
  }

  @Override
  protected PirObject visitGoto(Goto obj, Void param) {
    return new pir.statement.bbend.Goto((pir.cfg.BasicBlock) visit(obj.getTarget(), null));
  }

  @Override
  protected PirObject visitSsaVariable(SsaVariable obj, Void param) {
    return new pir.other.SsaVariable(obj.getName(), (TypeRef) visit(obj.getType(), null));
  }

  @Override
  protected PirObject visitTypeCast(TypeCast obj, Void param) {
    pir.other.SsaVariable var = (pir.other.SsaVariable) visit(obj.getVariable(), null);
    PirValue old = (PirValue) visit(obj.getValue(), null); // and we hope that we cast to the right value
    return new pir.statement.normal.convert.TypeCast(var, old);
  }

  @Override
  protected PirObject visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    pir.other.SsaVariable var = (pir.other.SsaVariable) visit(obj.getVariable(), null);
    ToVariableGenerator converter = new ToVariableGenerator(this);
    return (PirObject) converter.traverse(obj.getInit(), var);
  }

  @Override
  protected PirObject visitGetElementPtr(GetElementPtr obj, Void param) {
    // VarRef ref = (VarRef) traverse(obj.getAddress(), null);
    ArrayList<PirValue> ofs = new ArrayList<PirValue>();

    Variable baseAddr = (Variable) traverse(obj.getAddress().getLink(), null);

    pir.type.Type type = baseAddr.getType().getRef();
    /*
     * 
     * // hacky? assert ( type instanceof pir.type.PointerType ); type = ( (pir.type.PointerType) type
     * ).getType().getRef(); // if( ref.getRef() instanceof pir.other.StateVariable ) { // see llvm GEP FAQ: Why is the
     * extra 0 index required? ofs.add(new pir.expression.Number(BigInteger.ZERO, new TypeRef(kbi.getNoSignType(1))));
     * // }
     */

    for (evl.expression.reference.RefItem itr : obj.getAddress().getOffset()) {
      if (type instanceof StructType) {
        assert (itr instanceof RefName);
        // get index of struct member and use that
        StructType st = (StructType) type;
        String name = ((RefName) itr).getName();
        NamedElement elem = st.find(name);
        assert (elem != null);
        int idx = st.getElements().indexOf(elem);
        assert (idx >= 0);
        ofs.add(new pir.expression.Number(BigInteger.valueOf(idx), new TypeRef(kbi.getNoSignType(32))));
        // see llvm gep FAQ: Why do struct member indices always use i32?
        type = elem.getType().getRef();
      } else if (type instanceof pir.type.UnionType) {
        assert (itr instanceof RefName);
        // get index of struct member and use that
        pir.type.UnionType st = (pir.type.UnionType) type;
        String name = ((RefName) itr).getName();
        NamedElement elem = st.find(name);
        assert (elem != null);
        int idx = st.getElements().indexOf(elem);
        assert (idx >= 0);
        ofs.add(new pir.expression.Number(BigInteger.valueOf(idx), new TypeRef(kbi.getNoSignType(32))));
        // see llvm gep FAQ: Why do struct member indices always use i32?
        type = elem.getType().getRef();
      } else if (type instanceof pir.type.ArrayType) {
        assert (itr instanceof RefIndex);
        // get index calculation
        RefIndex idx = (RefIndex) itr;
        PirValue val = (PirValue) traverse(idx.getIndex(), null);
        ofs.add(val);
        type = ((pir.type.ArrayType) type).getType().getRef();
      } else if (type instanceof pir.type.PointerType) {
        assert (itr instanceof RefPtrDeref);
        // dereferencing is like accesing an array element
        ofs.add(new pir.expression.Number(BigInteger.ZERO, new TypeRef(kbi.getNoSignType(32))));
        // see llvm GEP FAQ: Why is the extra 0 index required?
        type = ((pir.type.PointerType) type).getType().getRef();
      } else {
        RError.err(ErrorType.Fatal, obj.getInfo(), "type: " + type.getClass().getCanonicalName());
      }
    }

    // TODO really needed anymore?
    PirValue base;
    if (baseAddr instanceof pir.other.StateVariable) {
      base = new VarRefStatevar((pir.other.StateVariable) baseAddr);
    } else if (baseAddr instanceof pir.other.SsaVariable) {
      base = new VarRefSimple((pir.other.SsaVariable) baseAddr);
    } else if (baseAddr instanceof pir.other.Constant) {
      base = new VarRefConst((pir.other.Constant) baseAddr);
    } else if (baseAddr instanceof pir.other.FuncVariable) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "not yet implemented " + baseAddr.getClass().getCanonicalName() + " -> implement alloca");
      // TODO implement stack memory allocation with alloca
      return null;
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "not yet implemented " + baseAddr.getClass().getCanonicalName());
      return null;
    }

    pir.other.SsaVariable var = (pir.other.SsaVariable) traverse(obj.getVariable(), null);

    return new pir.statement.normal.GetElementPtr(var, base, ofs);
  }

  @Override
  protected PirObject visitLoadStmt(LoadStmt obj, Void param) {
    pir.other.SsaVariable var = (pir.other.SsaVariable) traverse(obj.getVariable(), null);
    VarRefSimple ref = (VarRefSimple) traverse(obj.getAddress(), null);
    assert (ref.getType().getRef() instanceof pir.type.PointerType);
    return new pir.statement.normal.LoadStmt(var, ref);
  }

  @Override
  protected PirObject visitStoreStmt(evl.statement.normal.StoreStmt obj, Void param) {
    pir.other.PirValue value = (pir.other.PirValue) traverse(obj.getExpr(), null);
    VarRefSimple ref = (VarRefSimple) traverse(obj.getAddress(), null);
    assert (ref.getType().getRef() instanceof pir.type.PointerType);
    return new pir.statement.normal.StoreStmt(ref, value);
  }

  @Override
  protected PirObject visitAssignment(Assignment obj, Void param) {
    assert (obj.getLeft().getLink() instanceof evl.variable.Variable);
    evl.variable.Variable var = (evl.variable.Variable) obj.getLeft().getLink();

    if (isScalar(var.getType().getRef())) {
      assert (obj.getLeft().getOffset().isEmpty());

      ToVariableGenerator converter = new ToVariableGenerator(this);

      Variable dstvar = (Variable) visit(obj.getLeft().getLink(), null);
      if (dstvar instanceof pir.other.SsaVariable) {
        VariableGeneratorStmt vargen = converter.traverse(obj.getRight(), (pir.other.SsaVariable) dstvar);
        return (PirObject) vargen;
      } else if (dstvar instanceof pir.other.StateVariable) {
        PirValue src = (PirValue) visit(obj.getRight(), null);
        StoreStmt store = new StoreStmt(new VarRefStatevar((pir.other.StateVariable) dstvar), src);
        return store;
      } else {
        throw new RuntimeException("not yet implemented: " + dstvar.getClass().getCanonicalName());
      }
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Unexpected case: " + var.getType().getClass().getCanonicalName());
      return null;
    }
  }

  // TODO make it better
  private boolean isScalar(Type type) {
    return (type instanceof IntegerType) || (type instanceof BooleanType) || (type instanceof RangeType);
  }

  @Override
  protected PirObject visitTypeRef(evl.type.TypeRef obj, Void param) {
    return new TypeRef((pir.type.Type) visit(obj.getRef(), null));
  }
}

class ToVariableGenerator extends NullTraverser<VariableGeneratorStmt, pir.other.SsaVariable> {

  private ToPir converter;

  public ToVariableGenerator(ToPir converter) {
    super();
    this.converter = converter;
  }

  @Override
  protected VariableGeneratorStmt visitDefault(Evl obj, pir.other.SsaVariable param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected VariableGeneratorStmt visitReference(Reference obj, pir.other.SsaVariable param) {
    // TODO make it simple
    if ((obj.getLink() instanceof evl.variable.SsaVariable) && obj.getOffset().isEmpty()) {
      pir.other.SsaVariable var = (pir.other.SsaVariable) converter.traverse(obj.getLink(), null);
      return new pir.statement.normal.Assignment(param, new VarRefSimple(var));
    } else if (obj.getLink() instanceof FunctionHeader) {
      assert (obj.getOffset().size() == 1);
      evl.expression.reference.RefCall ofs = (RefCall) obj.getOffset().get(0);
      Function ref = (Function) converter.traverse(obj.getLink(), null);
      ArrayList<PirValue> parameter = new ArrayList<PirValue>();
      for (Expression expr : ofs.getActualParameter()) {
        parameter.add((PirValue) converter.traverse(expr, null));
      }
      return new CallAssignment(param, ref, parameter);
    } else if ((obj.getLink() instanceof evl.variable.StateVariable) && obj.getOffset().isEmpty()) {
      pir.other.StateVariable var = (pir.other.StateVariable) converter.traverse(obj.getLink(), null);
      return new pir.statement.normal.LoadStmt(param, new VarRefStatevar(var));
    } else {
      throw new RuntimeException("not yet implemented: " + obj);
    }
  }

  @Override
  protected VariableGeneratorStmt visitBoolValue(BoolValue obj, pir.other.SsaVariable param) {
    pir.type.BooleanType type = converter.kbi.getBooleanType();
    pir.expression.BoolValue num = new pir.expression.BoolValue(obj.isValue(), new TypeRef(type));
    return new pir.statement.normal.Assignment(param, num);
  }

  @Override
  protected VariableGeneratorStmt visitNumber(Number obj, pir.other.SsaVariable param) {
    pir.type.Type type = converter.kbi.getRangeType(obj.getValue(), obj.getValue());
    pir.expression.Number num = new pir.expression.Number(obj.getValue(), new TypeRef(type));
    return new pir.statement.normal.Assignment(param, num);
  }

  @Override
  protected VariableGeneratorStmt visitTypeCast(TypeCast obj, pir.other.SsaVariable param) {
    VarRefSimple old = (VarRefSimple) converter.traverse(obj.getValue(), null);
    return new pir.statement.normal.convert.TypeCast(param, old);
  }

  @Override
  protected VariableGeneratorStmt visitNot(Not obj, pir.other.SsaVariable param) {
    return new pir.statement.normal.unop.Not(param, (PirValue) converter.visit(obj.getExpr(), null));
  }

  @Override
  protected VariableGeneratorStmt visitAnd(And obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    if (left.getType().getRef() instanceof pir.type.BooleanType) {
      assert (right.getType().getRef() instanceof pir.type.BooleanType);
      return new pir.statement.normal.binop.LogicAnd(param, left, right);
    } else {
      assert (left.getType().getRef() instanceof pir.type.RangeType);
      assert (right.getType().getRef() instanceof pir.type.RangeType);
      return new pir.statement.normal.binop.BitAnd(param, left, right);
    }
  }

  @Override
  protected VariableGeneratorStmt visitDiv(Div obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Div(param, left, right);
  }

  @Override
  protected VariableGeneratorStmt visitMinus(Minus obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Minus(param, left, right);
  }

  @Override
  protected VariableGeneratorStmt visitMod(Mod obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Mod(param, left, right);
  }

  @Override
  protected VariableGeneratorStmt visitMul(Mul obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Mul(param, left, right);
  }

  @Override
  protected VariableGeneratorStmt visitOr(Or obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    if (left.getType().getRef() instanceof pir.type.BooleanType) {
      assert (right.getType().getRef() instanceof pir.type.BooleanType);
      return new pir.statement.normal.binop.LogicOr(param, left, right);
    } else {
      assert (left.getType().getRef() instanceof pir.type.RangeType);
      assert (right.getType().getRef() instanceof pir.type.RangeType);
      return new pir.statement.normal.binop.BitOr(param, left, right);
    }
  }

  @Override
  protected VariableGeneratorStmt visitPlus(Plus obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Plus(param, left, right);
  }

  @Override
  protected VariableGeneratorStmt visitShl(Shl obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Shl(param, left, right);
  }

  @Override
  protected VariableGeneratorStmt visitShr(Shr obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Shr(param, left, right);
  }

  @Override
  protected VariableGeneratorStmt visitEqual(Equal obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    if (left.getType().getRef() instanceof pir.type.BooleanType) {
      assert (right.getType().getRef() instanceof pir.type.BooleanType);
      return new pir.statement.normal.binop.LogicXand(param, left, right);
    } else {
      assert (left.getType().getRef() instanceof pir.type.RangeType);
      assert (right.getType().getRef() instanceof pir.type.RangeType);
      return new pir.statement.normal.binop.Equal(param, left, right);
    }
  }

  @Override
  protected VariableGeneratorStmt visitGreater(Greater obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Greater(param, left, right);
  }

  @Override
  protected VariableGeneratorStmt visitNotequal(Notequal obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    if (left.getType().getRef() instanceof pir.type.BooleanType) {
      assert (right.getType().getRef() instanceof pir.type.BooleanType);
      return new pir.statement.normal.binop.LogicXor(param, left, right);
    } else {
      assert (left.getType().getRef() instanceof pir.type.RangeType);
      assert (right.getType().getRef() instanceof pir.type.RangeType);
      return new pir.statement.normal.binop.Notequal(param, left, right);
    }
  }

  @Override
  protected VariableGeneratorStmt visitGreaterequal(Greaterequal obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Greaterequal(param, left, right);
  }

  @Override
  protected VariableGeneratorStmt visitLess(Less obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Less(param, left, right);
  }

  @Override
  protected VariableGeneratorStmt visitLessequal(Lessequal obj, pir.other.SsaVariable param) {
    // TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.normal.binop.Lessequal(param, left, right);
  }
}
