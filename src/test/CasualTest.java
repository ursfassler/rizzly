package test;

import org.junit.Test;

public class CasualTest extends BaseTest {
  private static final boolean DEBUG_EVENT = false;   //TODO set to true

  public CasualTest() {
    super();
    setStrict(false);
  }

  @Override
  protected String getRootdir() {
    return "casual/";
  }

  @Test
  public void stateVar() {
    compile("stateVar", TestSteps.EXECUTE, DEBUG_EVENT, false);
  }

  @Test
  public void stateVar1() {
    compile("stateVar1", TestSteps.EXECUTE, DEBUG_EVENT, false);
  }

  @Test
  public void stateVar2() {
    compile("stateVar2", TestSteps.EXECUTE, DEBUG_EVENT, false);
  }

  @Test
  public void rec() {
    compile("rec", TestSteps.EXECUTE, DEBUG_EVENT, false);
  }

  @Test
  public void rec1() {
    compile("rec1", TestSteps.EXECUTE, DEBUG_EVENT, false);
  }

  @Test
  public void range() {
    compile("range", TestSteps.EXECUTE, DEBUG_EVENT, false);
  }

  @Test
  public void meta() {
    compile("meta", "Meta", true, DEBUG_EVENT, false);
  }

  @Test
  public void linking() {
    compile("linking", "Linking", true, DEBUG_EVENT, false);
  }

  @Test
  public void bitred() {
    compile("bitred", "Bitred", true, DEBUG_EVENT, false);
  }

  @Test
  public void arrayTest_ArrayTest() {
    compile("arrayTest", "ArrayTest", true, DEBUG_EVENT, false);
  }

  @Test
  public void bool_Bool() {
    compile("bool", "Bool", true, DEBUG_EVENT, false);
  }

  @Test
  public void bool2_Bool2() {
    compile("bool2", "Bool2", true, DEBUG_EVENT, false);
  }

  @Test
  public void case_Case() {
    compile("case", "Case", true, DEBUG_EVENT, false);
  }

  @Test
  public void calcCase_CalcCase() {
    compile("calcCase", "CalcCase", true, DEBUG_EVENT, false);
  }

  // @Test
  // public void compFib() {
  // compile("compFib", "CompFib", true, DEBUG_EVENT, false);
  // }

  @Test
  public void composition_Composition() {
    compile("composition", "Composition", true, DEBUG_EVENT, false);
  }

  @Test
  public void compositionQuery_CompositionQuery() {
    compile("compositionQuery", "CompositionQuery", true, DEBUG_EVENT, true);
  }

  @Test
  public void constdef() {
    compile("constdef", "Constdef", true, DEBUG_EVENT, false);
  }

  @Test
  public void downCast_DownCast() {
    compile("downCast", "DownCast", true, DEBUG_EVENT, false);
  }

  @Test
  public void elemInit() {
    compile("elemInit", "ElemInit", true, DEBUG_EVENT, false);
  }

  @Test
  public void expr_Expr() {
    compile("expr", "Expr", true, DEBUG_EVENT, true);
  }

  @Test
  public void expr2_Expr2() {
    compile("expr2", "Expr2", true, DEBUG_EVENT, false);
  }

  @Test
  public void extConst() {
    compile("extConst", "ExtConst", true, DEBUG_EVENT, false);
  }

  @Test
  public void constInit_ConstInit() {
    compile("constInit", "ConstInit", true, DEBUG_EVENT, false);
  }

  @Test
  public void enumCase_Case() {
    compile("enumCase", "EnumCase", true, DEBUG_EVENT, false);
  }

  @Test
  public void enumTest_EnumTest() {
    compile("enumTest", "EnumTest", true, DEBUG_EVENT, false);
  }

  @Test
  public void genericUse2() {
    compile("genericUse2", "GenericUse2", true, DEBUG_EVENT, false);
  }

  @Test
  public void genericUse_GenericUse() {
    compile("genericUse", "GenericUse", true, DEBUG_EVENT, false);
  }

  @Test
  public void genIface_GenIface() {
    compile("genIface", "GenIface", true, DEBUG_EVENT, true);
  }

  // @Test
  // public void gfunc_GFunc() {
  // compile("gfunc", "GFunc", true, DEBUG_EVENT, false);
  // }

  @Test
  public void ifacemap() {
    compile("ifacemap", "Ifacemap", true, DEBUG_EVENT, false);
  }

  @Test
  public void linkorder() {
    compile("linkorder", "Linkorder", true, DEBUG_EVENT, false);
  }

  @Test
  public void linkorder2() {
    compile("linkorder2", "Linkorder2", true, DEBUG_EVENT, false);
  }

  @Test
  public void unionTest_UnionTest() {
    compile("unionTest", "UnionTest", true, DEBUG_EVENT, false);
  }

  @Test
  public void smallBitTest_SmallBitTest() {
    compile("smallBitTest", "SmallBitTest", true, DEBUG_EVENT, false);
  }

  @Test
  public void smallGenericUse_SmallGenericUse() {
    compile("smallGenericUse", "SmallGenericUse", true, DEBUG_EVENT, false);
  }

  @Test
  public void typeAlias_TypeAlias() {
    compile("typeAlias", "TypeAlias", true, DEBUG_EVENT, false);
  }

  @Test
  public void typefunc() {
    compile("typefunc", "Typefunc", true, DEBUG_EVENT, false);
  }

  @Test
  public void varInit_VarInit() {
    compile("varInit", "VarInit", true, DEBUG_EVENT, false);
  }

  @Test
  public void while_() {
    compile("while", "While", true, DEBUG_EVENT, true);
  }

  @Test
  public void while2() {
    compile("while2", "While2", true, DEBUG_EVENT, true);
  }

  @Test
  public void retval_Retval() {
    compile("retval", "Retval", true, DEBUG_EVENT, true);
  }

  @Test
  public void ui_Ui() {
    compile("ui", "Ui", true, DEBUG_EVENT, false);
  }

  @Test
  public void alarmController() {
    compile("alarmController", "AlarmController", true, DEBUG_EVENT, false);
  }

  @Test
  public void param() {
    compile("param", "Param", true, DEBUG_EVENT, false);
  }
}
