package evl.traverser;

import common.ElementInfo;
import evl.statement.GetElementPtr;
import evl.statement.LoadStmt;
import evl.statement.StackMemoryAlloc;
import evl.type.special.PointerType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pir.PirObject;
import pir.cfg.BasicBlockEnd;
import pir.cfg.CaseOptEntry;
import pir.expression.PExpression;
import pir.expression.UnOp;
import pir.expression.reference.RefItem;
import pir.expression.reference.VarRef;
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
import pir.statement.ArOp;
import pir.statement.CallAssignment;
import pir.statement.StoreStmt;
import pir.statement.VariableGeneratorStmt;
import pir.type.NamedElement;
import pir.type.RangeType;
import pir.type.TypeRef;
import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.CaseGoto;
import evl.cfg.CaseGotoOpt;
import evl.cfg.CaseOptRange;
import evl.cfg.CaseOptValue;
import evl.cfg.Goto;
import evl.cfg.IfGoto;
import evl.cfg.PhiStmt;
import evl.cfg.ReturnExpr;
import evl.cfg.ReturnVoid;
import evl.expression.ArithmeticOp;
import evl.expression.ArrayValue;
import evl.expression.BoolValue;
import evl.expression.ExpOp;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.Relation;
import evl.expression.StringValue;
import evl.expression.TypeCast;
import evl.expression.UnaryExpression;
import evl.expression.UnaryOp;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.RefPtrDeref;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.function.FunctionHeader;
import evl.other.RizzlyProgram;
import evl.statement.Assignment;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.statement.VarDefInitStmt;
import evl.statement.VarDefStmt;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.Range;
import evl.type.base.StringType;
import evl.type.composed.RecordType;
import evl.type.special.IntegerType;
import evl.type.special.VoidType;
import evl.variable.Constant;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
import evl.variable.StateVariable;
import java.math.BigInteger;
import pir.expression.reference.VarRefConst;
import pir.traverser.ExprTypeGetter;
import pir.type.StructType;
import util.ssa.PhiInserter;

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
    if( cobj == null ) {
      cobj = super.visit(obj, param);
      assert ( cobj != null );
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected PirObject visitReference(Reference obj, Void param) {
    PirObject ref = visit(obj.getLink(), null);
    if( ref instanceof Variable ) {
      if( ( ref instanceof pir.other.SsaVariable ) && ( obj.getOffset().isEmpty() ) ) {
        return new VarRefSimple((pir.other.SsaVariable) ref);
      } else {
        ArrayList<RefItem> offset = new ArrayList<RefItem>();
        for( evl.expression.reference.RefItem itm : obj.getOffset() ) {
          offset.add((RefItem) visit(itm, null));
        }
        return new VarRef((Variable) ref, offset);
      }
    } else if( ref instanceof pir.type.Type ) {
      assert ( obj.getOffset().isEmpty() );
      return new TypeRef((pir.type.Type) ref);
    } else if( ref instanceof Function ) {
      assert ( ( obj.getOffset().size() == 1 ) && ( obj.getOffset().get(0) instanceof RefCall ) );
      RefCall call = (RefCall) obj.getOffset().get(0);

      ArrayList<PirValue> acpa = new ArrayList<PirValue>();
      for( Expression pa : call.getActualParameter() ) {
        acpa.add((PirValue) visit(pa, null));
      }

      return new pir.statement.CallStmt((Function) ref, acpa); // TODO correct? return value not used?
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

    kbi = ( new KnowledgeBase(prog, rootdir) ).getEntry(KnowBaseItem.class);
    for( Type type : obj.getType() ) {
      pir.type.Type ct = (pir.type.Type) visit(type, param);
      if( !prog.getType().contains(ct) ) {    //TODO why?
        prog.getType().add(ct);
      }
    }
    for( StateVariable itr : obj.getVariable() ) {
      pir.other.StateVariable ct = (pir.other.StateVariable) visit(itr, param);
      prog.getVariable().add(ct);
    }
    for( Constant itr : obj.getConstant() ) {
      pir.other.Constant ct = (pir.other.Constant) visit(itr, param);
      prog.getConstant().add(ct);
    }
    for( FunctionBase itr : obj.getFunction() ) {
      pir.function.Function ct = (pir.function.Function) visit(itr, param);
      prog.getFunction().add(ct);
    }
    return prog;
  }

  @Override
  protected PirObject visitFunctionBase(FunctionBase obj, Void param) {
    String name = obj.getName();

    List<pir.other.SsaVariable> arg = new ArrayList<pir.other.SsaVariable>();
    for( evl.variable.Variable var : obj.getParam() ) {
      assert ( var instanceof SsaVariable );
      arg.add((pir.other.SsaVariable) visit(var, param));
    }

    pir.function.Function func;
    TypeRef retType;
    if( obj instanceof FuncWithReturn ) {
      retType = (TypeRef) visit(( (FuncWithReturn) obj ).getRet(), null);
    } else {
      retType = new TypeRef(kbi.getVoidType());
    }

    if( obj instanceof FuncWithBody ) {
      func = new FuncImpl(name, arg, retType);
    } else {
      func = new FuncProto(name, arg, retType);
    }
    func.getAttributes().addAll(obj.getAttributes());

    map.put(obj, func); // otherwise the compiler follows recursive calls

    if( obj instanceof FuncWithBody ) {
      pir.cfg.BasicBlockList stmt = (pir.cfg.BasicBlockList) visit(( (FuncWithBody) obj ).getBody(), param);
      ( (pir.function.FuncWithBody) func ).setBody(stmt);
    }

    return func;
  }

  @Override
  protected pir.type.StructType visitRecordType(RecordType obj, Void param) {
    pir.type.StructType struct = new pir.type.StructType(obj.getName());
    for( evl.type.composed.NamedElement elem : obj.getElement() ) {
      TypeRef type = (TypeRef) visit(elem.getType(), param);
      NamedElement celem = new NamedElement(elem.getName(), type);
      struct.getElements().add(celem);
    }
    return struct;
  }

  @Override
  protected pir.type.EnumType visitEnumType(EnumType obj, Void param) {
    pir.type.EnumType struct = new pir.type.EnumType(obj.getName());
    map.put(obj, struct);
    for( EnumElement elem : obj.getElement() ) {
      pir.type.EnumElement celem = (pir.type.EnumElement) visit(elem, struct);
      struct.getElements().add(celem);
    }
    return struct;
  }

  @Override
  protected pir.type.EnumElement visitEnumElement(EnumElement obj, Void param) {
    pir.type.EnumType type = (pir.type.EnumType) param;
    return new pir.type.EnumElement(obj.getName(), type);
  }

  @Override
  protected PirObject visitVarDef(VarDefStmt obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "VarDefStmt should no longer exists; replaced with StackMemoryAlloc");
    return null;
  }

  @Override
  protected PirObject visitStackMemoryAlloc(StackMemoryAlloc obj, Void param) {
    pir.other.SsaVariable var = (pir.other.SsaVariable) visit(obj.getVariable(), null);
    pir.statement.StackMemoryAlloc sma = new pir.statement.StackMemoryAlloc(var);
    return sma;
  }

  @Override
  protected pir.statement.CallStmt visitCallStmt(CallStmt obj, Void param) {
    PirObject call = visit(obj.getCall(), param);
    return (pir.statement.CallStmt) call;
    // assert (call instanceof CallAssignment);
    // CallAssignment ass = (CallAssignment) call;
    // return new pir.statement.CallStmt(ass.getRef(), ass.getParameter()); // FIXME is that ok? just forget the defined
    // // variable?
  }

  @Override
  protected PirObject visitBasicBlockList(BasicBlockList obj, Void param) {
    pir.cfg.BasicBlockList ret = new pir.cfg.BasicBlockList((pir.cfg.BasicBlock) visit(obj.getEntry(), null), (pir.cfg.BasicBlock) visit(obj.getExit(), null));
    for( BasicBlock bb : obj.getBasicBlocks() ) {
      ret.getBasicBlocks().add((pir.cfg.BasicBlock) visit(bb, param));
    }
    return ret;
  }

  private UnOp toUnOp(UnaryOp op) {
    switch( op ) {
      case MINUS:
        return pir.expression.UnOp.MINUS;
      case NOT:
        return pir.expression.UnOp.NOT;
      default:
        throw new RuntimeException("not yet implemented: " + op);
    }
  }

  @Override
  protected PirObject visitBoolValue(BoolValue obj, Void param) {
    return new pir.expression.BoolValue(obj.isValue());
  }

  @Override
  protected pir.expression.Number visitNumber(Number obj, Void param) {
    RangeType type = kbi.getRangeType(obj.getValue(), obj.getValue());
    return new pir.expression.Number(obj.getValue(), new TypeRef(type));
  }

  @Override
  protected PirObject visitStringValue(StringValue obj, Void param) {
    return new pir.expression.StringValue(obj.getValue());
  }

  @Override
  protected PirObject visitArrayValue(ArrayValue obj, Void param) {
    pir.expression.ArrayValue ret = new pir.expression.ArrayValue();
    for( Expression itr : obj.getValue() ) {
      PirObject arg = visit(itr, param);
      assert ( arg instanceof PExpression );
      ret.getValue().add((PExpression) arg);
    }
    return ret;
  }

  @Override
  protected PirObject visitPointerType(PointerType obj, Void param) {
    return new pir.type.PointerType((TypeRef) traverse(obj.getType(), null));
  }

  @Override
  protected PirObject visitRange(Range obj, Void param) {
    pir.type.RangeType ret = kbi.getRangeType(obj.getLow(), obj.getHigh());
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
    assert ( expr instanceof PirValue );
    return new pir.cfg.ReturnExpr((PirValue) expr);
  }

  @Override
  protected PirObject visitReturnVoid(ReturnVoid obj, Void param) {
    return new pir.cfg.ReturnVoid();
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
  protected PirObject visitCaseOptRange(CaseOptRange obj, Void param) {
    assert ( obj.getStart() instanceof Number );
    assert ( obj.getEnd() instanceof Number );
    return new pir.cfg.CaseOptRange(( (Number) obj.getStart() ).getValue(), ( (Number) obj.getEnd() ).getValue());
  }

  @Override
  protected PirObject visitCaseOptValue(CaseOptValue obj, Void param) {
    assert ( obj.getValue() instanceof Number );
    return new pir.cfg.CaseOptValue((pir.expression.Number) visit(obj.getValue(), null));
  }

  @Override
  protected PirObject visitPhiStmt(PhiStmt obj, Void param) {
    pir.cfg.PhiStmt ret = new pir.cfg.PhiStmt((pir.other.SsaVariable) visit(obj.getVariable(), null));
    for( BasicBlock in : obj.getInBB() ) {
      pir.other.SsaVariable var = (pir.other.SsaVariable) visit(obj.getArg(in), null);
      ret.addArg((pir.cfg.BasicBlock) visit(in, null), new VarRefSimple(var));
    }
    return ret;
  }

  @Override
  protected PirObject visitBasicBlock(BasicBlock obj, Void param) {
    pir.cfg.BasicBlock ret = new pir.cfg.BasicBlock(obj.getName());
    map.put(obj, ret);

    for( PhiStmt phi : obj.getPhi() ) {
      ret.getPhi().add((pir.cfg.PhiStmt) visit(phi, null));
    }
    for( Statement stmt : obj.getCode() ) {
      ret.getCode().add((pir.statement.Statement) visit(stmt, null));
    }
    ret.setEnd((BasicBlockEnd) visit(obj.getEnd(), null));

    return ret;
  }

  @Override
  protected PirObject visitCaseGoto(CaseGoto obj, Void param) {
    PirObject cond = visit(obj.getCondition(), null);
    assert ( cond instanceof VarRefSimple );
    pir.other.SsaVariable var = ( (VarRefSimple) cond ).getRef();
    pir.cfg.CaseGoto caseGoto = new pir.cfg.CaseGoto(new VarRefSimple(var), (pir.cfg.BasicBlock) visit(obj.getOtherwise(), null));
    for( CaseGotoOpt opt : obj.getOption() ) {
      pir.cfg.CaseGotoOpt nopt = (pir.cfg.CaseGotoOpt) visit(opt, null);
      caseGoto.getOption().add(nopt);
    }
    return caseGoto;
  }

  @Override
  protected PirObject visitCaseGotoOpt(CaseGotoOpt obj, Void param) {
    List<CaseOptEntry> list = new ArrayList<CaseOptEntry>();
    for( evl.cfg.CaseOptEntry entry : obj.getValue() ) {
      list.add((CaseOptEntry) visit(entry, null));
    }
    return new pir.cfg.CaseGotoOpt(list, (pir.cfg.BasicBlock) visit(obj.getDst(), null));
  }

  @Override
  protected PirObject visitIfGoto(IfGoto obj, Void param) {
    PirObject cond = visit(obj.getCondition(), null);
    assert ( cond instanceof VarRefSimple );
    pir.other.SsaVariable var = ( (VarRefSimple) cond ).getRef();
    assert ( var.getType().getRef() instanceof pir.type.BooleanType );
    return new pir.cfg.IfGoto(new VarRefSimple(var), (pir.cfg.BasicBlock) visit(obj.getThenBlock(), null), (pir.cfg.BasicBlock) visit(obj.getElseBlock(), null));
  }

  @Override
  protected PirObject visitGoto(Goto obj, Void param) {
    return new pir.cfg.Goto((pir.cfg.BasicBlock) visit(obj.getTarget(), null));
  }

  @Override
  protected PirObject visitSsaVariable(SsaVariable obj, Void param) {
    return new pir.other.SsaVariable(obj.getName(), (TypeRef) visit(obj.getType(), null));
  }

  @Override
  protected PirObject visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    pir.other.SsaVariable var = (pir.other.SsaVariable) visit(obj.getVariable(), null);
    ToVariableGenerator converter = new ToVariableGenerator(this);
    return converter.traverse(obj.getInit(), var);
  }

  @Override
  protected PirObject visitGetElementPtr(GetElementPtr obj, Void param) {
//    VarRef ref = (VarRef) traverse(obj.getAddress(), null);
    ArrayList<PirValue> ofs = new ArrayList<PirValue>();

    Variable baseAddr = (Variable) traverse(obj.getAddress().getLink(), null);

    pir.type.Type type = baseAddr.getType().getRef();
    /*
    
    // hacky?
    assert ( type instanceof pir.type.PointerType );
    type = ( (pir.type.PointerType) type ).getType().getRef();
    //    if( ref.getRef() instanceof pir.other.StateVariable ) {
    // see llvm GEP FAQ: Why is the extra 0 index required?
    ofs.add(new pir.expression.Number(BigInteger.ZERO, new TypeRef(kbi.getNoSignType(1))));
    //    }
     */

    for( evl.expression.reference.RefItem itr : obj.getAddress().getOffset() ) {
      if( itr instanceof RefName ) {
        // get index of struct member and use that
        StructType st = (StructType) type;
        String name = ( (RefName) itr ).getName();
        NamedElement elem = st.find(name);
        assert ( elem != null );
        int idx = st.getElements().indexOf(elem);
        assert ( idx >= 0 );
        ofs.add(new pir.expression.Number(BigInteger.valueOf(idx), new TypeRef(kbi.getNoSignType(32))));
        //see llvm gep FAQ: Why do struct member indices always use i32?
        type = elem.getType().getRef();
      } else if( itr instanceof RefIndex ) {
        // get index calculation
        RefIndex idx = (RefIndex) itr;
        PirValue val = (PirValue) traverse(idx.getIndex(), null);
        ofs.add(val);
        type = ( (pir.type.ArrayType) type ).getType().getRef();
      } else if( itr instanceof RefPtrDeref ) {
        // dereferencing is like accesing an array element
        ofs.add(new pir.expression.Number(BigInteger.ZERO, new TypeRef(kbi.getNoSignType(32))));
        // see llvm GEP FAQ: Why is the extra 0 index required?
        type = ( (pir.type.PointerType) type ).getType().getRef();
      } else {
        RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled offset item: " + itr.getClass().getCanonicalName());
      }
    }

    //TODO really needed anymore?
    PirValue base;
    if( baseAddr instanceof pir.other.StateVariable ) {
      base = new VarRefStatevar((pir.other.StateVariable) baseAddr);
    } else if( baseAddr instanceof pir.other.SsaVariable ) {
      base = new VarRefSimple((pir.other.SsaVariable) baseAddr);
    } else if( baseAddr instanceof pir.other.Constant ) {
      base = new VarRefConst((pir.other.Constant) baseAddr);
    } else if( baseAddr instanceof pir.other.FuncVariable ) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "not yet implemented " + baseAddr.getClass().getCanonicalName() + " -> implement alloca");
      //TODO implement stack memory allocation with alloca
      return null;
    } else {
      RError.err(ErrorType.Fatal, obj.getInfo(), "not yet implemented " + baseAddr.getClass().getCanonicalName());
      return null;
    }

    pir.other.SsaVariable var = (pir.other.SsaVariable) traverse(obj.getVariable(), null);

    return new pir.statement.GetElementPtr(var, base, ofs);
  }

  @Override
  protected PirObject visitLoadStmt(LoadStmt obj, Void param) {
    pir.other.SsaVariable var = (pir.other.SsaVariable) traverse(obj.getVariable(), null);
    VarRefSimple ref = (VarRefSimple) traverse(obj.getAddress(), null);
    assert ( ref.getType().getRef() instanceof pir.type.PointerType );
    return new pir.statement.LoadStmt(var, ref);
  }

  @Override
  protected PirObject visitStoreStmt(evl.statement.StoreStmt obj, Void param) {
    pir.other.PirValue value = (pir.other.PirValue) traverse(obj.getExpr(), null);
    VarRefSimple ref = (VarRefSimple) traverse(obj.getAddress(), null);
    assert ( ref.getType().getRef() instanceof pir.type.PointerType );
    return new pir.statement.StoreStmt(ref, value);
  }

  @Override
  protected PirObject visitAssignment(Assignment obj, Void param) {
    assert ( obj.getLeft().getLink() instanceof evl.variable.Variable );
    evl.variable.Variable var = (evl.variable.Variable) obj.getLeft().getLink();

    if( isScalar(var.getType().getRef()) ) {
      assert ( obj.getLeft().getOffset().isEmpty() );

      ToVariableGenerator converter = new ToVariableGenerator(this);

      Variable dstvar = (Variable) visit(obj.getLeft().getLink(), null);
      if( dstvar instanceof pir.other.SsaVariable ) {
        VariableGeneratorStmt vargen = converter.traverse(obj.getRight(), (pir.other.SsaVariable) dstvar);
        return vargen;
      } else if( dstvar instanceof pir.other.StateVariable ) {
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
    return ( type instanceof IntegerType ) || ( type instanceof BooleanType ) || ( type instanceof Range );
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
    if( ( obj.getLink() instanceof evl.variable.SsaVariable ) && obj.getOffset().isEmpty() ) {
      pir.other.SsaVariable var = (pir.other.SsaVariable) converter.traverse(obj.getLink(), null);
      return new pir.statement.Assignment(param, new VarRefSimple(var));
    } else if( obj.getLink() instanceof FunctionHeader ) {
      assert ( obj.getOffset().size() == 1 );
      evl.expression.reference.RefCall ofs = (RefCall) obj.getOffset().get(0);
      Function ref = (Function) converter.traverse(obj.getLink(), null);
      ArrayList<PirValue> parameter = new ArrayList<PirValue>();
      for( Expression expr : ofs.getActualParameter() ) {
        parameter.add((PirValue) converter.traverse(expr, null));
      }
      return new CallAssignment(param, ref, parameter);
    } else if( ( obj.getLink() instanceof evl.variable.StateVariable ) && obj.getOffset().isEmpty() ) {
      pir.other.StateVariable var = (pir.other.StateVariable) converter.traverse(obj.getLink(), null);
      return new pir.statement.LoadStmt(param, new VarRefStatevar(var));
    } else {
      throw new RuntimeException("not yet implemented: " + obj);
    }
  }

  @Override
  protected VariableGeneratorStmt visitNumber(Number obj, pir.other.SsaVariable param) {
    pir.type.Type type = converter.kbi.getRangeType(obj.getValue(), obj.getValue());
    pir.expression.Number num = new pir.expression.Number(obj.getValue(), new TypeRef(type));
    return new pir.statement.Assignment(param, num);
  }

  @Override
  protected VariableGeneratorStmt visitTypeCast(TypeCast obj, pir.other.SsaVariable param) {
    VarRefSimple old = (VarRefSimple) converter.traverse(obj.getRef(), null);
    return new pir.statement.convert.TypeCast(param, old);
  }

  @Override
  protected VariableGeneratorStmt visitRelation(Relation obj, pir.other.SsaVariable param) {
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.Relation(param, left, right, obj.getOp());
  }

  @Override
  protected VariableGeneratorStmt visitArithmeticOp(ArithmeticOp obj, pir.other.SsaVariable param) {
    //TODO do not use VarRef for composite types but already use getElementPtr (maybe)
    PirValue left = (PirValue) converter.visit(obj.getLeft(), null);
    PirValue right = (PirValue) converter.visit(obj.getRight(), null);
    return new pir.statement.ArithmeticOp(param, left, right, toCOp(obj.getOp()));
  }

  private ArOp toCOp(ExpOp op) {
    switch( op ) {
      case MUL:
        return ArOp.MUL;
      case PLUS:
        return ArOp.PLUS;
      case DIV:
        return ArOp.DIV;
      case MINUS:
        return ArOp.MINUS;
      case AND:
        return ArOp.AND;
      case OR:
        return ArOp.OR;
      case MOD:
        return ArOp.MOD;
      case SHL:
        return ArOp.SHL;
      case SHR:
        return ArOp.SHR;
      default: {
        throw new RuntimeException("not yet implemented: " + op);
      }
    }
  }

  @Override
  protected VariableGeneratorStmt visitUnaryExpression(UnaryExpression obj, pir.other.SsaVariable param) {
    PirValue right = (PirValue) converter.visit(obj.getExpr(), null);
    return new pir.statement.UnaryOp(param, right, obj.getOp());
  }
}
