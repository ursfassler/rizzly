package test;

import org.junit.Test;

public class CasualTest extends BaseTest {
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
    compile("stateVar", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void stateVar1() {
    compile("stateVar1", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void stateVar2() {
    compile("stateVar2", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void rec() {
    compile("rec", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void rec1() {
    compile("rec1", TestSteps.EXECUTE, false, false);
  }

  //TODO pass composite types by reference to functions, bring this tests to work
  /*
  @Test
  public void rec2() {
    compile("rec2", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void rec3() {
    compile("rec3", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void while_() {
    compile("while", TestSteps.EXECUTE, false, true);
  }
   */

  @Test
  public void range() {
    compile("range", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void smallGenericUse() {
    compile("smallGenericUse",TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void linkorder() {
    compile("linkorder", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void retval() {
    compile("retval", TestSteps.EXECUTE, false, true);
  }

  @Test
  public void composition() {
    compile("composition", TestSteps.EXECUTE, true, false);
  }

  @Test
  public void while2() {
    compile("while2", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void typefunc() {
    compile("typefunc", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void calcCase() {
    compile("calcCase", TestSteps.EXECUTE, false, false);
  }

  //TODO add type check/conversation to C interface
  @Test
  public void elemInit() {
    compile("elemInit", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void meta() {
    compile("meta", TestSteps.COMPILE_TO_EVL, false, false);
  }

  @Test
  public void case_() {
    compile("case", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void varInit() {
    compile("varInit", TestSteps.COMPILE_TO_PIR, false, false);
  }

  @Test
  public void typeAlias() {
    compile("typeAlias", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void downCast() {
    compile("downCast", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void ifacemap() {
    compile("ifacemap", TestSteps.EXECUTE, true, false);
  }

  @Test
  public void linkorder2() {
    compile("linkorder2", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void genIface() {
    compile("genIface", TestSteps.COMPILE_TO_BIN, false, true);
  }

  //TODO implement arrays
  @Test
  public void arrayTest_ArrayTest() {
    compile("arrayTest", TestSteps.EXECUTE, false, false);
  }

  
  
  
  
  
/*  
  @Test
  public void constInit_ConstInit() {
    compile("constInit", "ConstInit", true, false, false);
  }

  @Test
  public void ui_Ui() {
    compile("ui", "Ui", true, false, false);
  }

  @Test
  public void linking() {
    compile("linking", "Linking", true, false, false);
  }

  @Test
  public void bitred() {
    compile("bitred", "Bitred", true, false, false);
  }

  @Test
  public void bool_Bool() {
    compile("bool", "Bool", true, false, false);
  }

  @Test
  public void bool2_Bool2() {
    compile("bool2", "Bool2", true, false, false);
  }

  // @Test
  // public void compFib() {
  // compile("compFib", "CompFib", true, false, false);
  // }

  @Test
  public void compositionQuery_CompositionQuery() {
    compile("compositionQuery", "CompositionQuery", true, false, true);
  }

  @Test
  public void constdef() {
    compile("constdef", "Constdef", true, false, false);
  }

  @Test
  public void expr_Expr() {
    compile("expr", "Expr", true, false, true);
  }

  @Test
  public void expr2_Expr2() {
    compile("expr2", "Expr2", true, false, false);
  }

  @Test
  public void extConst() {
    compile("extConst", "ExtConst", true, false, false);
  }

  @Test
  public void enumCase_Case() {
    compile("enumCase", "EnumCase", true, false, false);
  }

  @Test
  public void enumTest_EnumTest() {
    compile("enumTest", "EnumTest", true, false, false);
  }

  @Test
  public void genericUse2() {
    compile("genericUse2", "GenericUse2", true, false, false);
  }

  @Test
  public void genericUse_GenericUse() {
    compile("genericUse", "GenericUse", true, false, false);
  }

   @Test
   public void gfunc_GFunc() {
   compile("gfunc", "GFunc", true, false, false);
   }

  @Test
  public void unionTest_UnionTest() {
    compile("unionTest", "UnionTest", true, false, false);
  }

  @Test
  public void smallBitTest_SmallBitTest() {
    compile("smallBitTest", "SmallBitTest", true, false, false);
  }

  @Test
  public void alarmController() {
    compile("alarmController", "AlarmController", true, false, false);
  }

  @Test
  public void param() {
    compile("param", "Param", true, false, false);
  }
   */
}
