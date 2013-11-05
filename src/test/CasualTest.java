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
  public void mul() {
    compile("mul", TestSteps.EXECUTE, false, false);
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

  // TODO pass composite types by reference to functions, bring this tests to work
  /*
   * @Test public void rec2() { compile("rec2", TestSteps.EXECUTE, false, false); }
   * 
   * @Test public void rec3() { compile("rec3", TestSteps.EXECUTE, false, false); }
   * 
   * @Test public void while_() { compile("while", TestSteps.EXECUTE, false, true); }
   */
  @Test
  public void range() {
    compile("range", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void smallGenericUse() {
    compile("smallGenericUse", TestSteps.COMPILE_TO_ASM, false, false);
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
  public void debugMsg() {
    compile("debugMsg", TestSteps.EXECUTE, true, false);
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

  // TODO add type check/conversation to C interface
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
    compile("ifacemap", TestSteps.COMPILE_TO_ASM, true, false);
  }

  @Test
  public void linkorder2() {
    compile("linkorder2", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void genIface() {
    compile("genIface", TestSteps.COMPILE_TO_BIN, false, true);
  }

  @Test
  public void arrayTest1() {
    compile("arrayTest1", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest2() {
    compile("arrayTest2", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest3() {
    compile("arrayTest3", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void arrayTest() {
    compile("arrayTest", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void constInit() {
    compile("constInit", TestSteps.COMPILE_TO_LLVM, false, false);
  }

  @Test
  public void alarmController() {
    compile("alarmController", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void enumCase() {
    compile("enumCase", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void enumTest() {
    compile("enumTest", TestSteps.COMPILE_TO_FUN, false, false);
  }

  @Test
  public void elemComp() {
    compile("elemComp", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void transition1() {
    compile("transition1", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void alarmClock() {
    compile("alarmClock", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void union1() {
    compile("union1", TestSteps.COMPILE_TO_ASM, false, false);// TODO make testcase
  }

  @Test
  public void constdef() {
    compile("constdef", TestSteps.COMPILE_TO_PIR, false, false);
  }

  @Test
  public void extConst() {
    compile("extConst", TestSteps.COMPILE_TO_PIR, false, false);
  }

  @Test
  public void genericUse2() {
    compile("genericUse2", TestSteps.COMPILE_TO_PIR, false, false);
  }

  @Test
  public void genericUse() {
    compile("genericUse", TestSteps.COMPILE_TO_PIR, false, false);
  }

  @Test
  public void unionTest_UnionTest() {
    compile("unionTest", TestSteps.COMPILE_TO_ASM, false, false); // TODO make testcase
  }

  @Test
  public void elementary1() {
    compile("elementary1", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void elementary2() {
    compile("elementary2", TestSteps.COMPILE_TO_BIN, false, false);
  }

  @Test
  public void elementary3() {
    compile("elementary3", TestSteps.COMPILE_TO_BIN, false, false);
  }

  @Test
  public void elementary4() {
    compile("elementary4", TestSteps.COMPILE_TO_BIN, false, false);
  }

  @Test
  public void ui() {
    compile("ui", TestSteps.COMPILE_TO_ASM, false, false); // TODO make real testcase
  }

  @Test
  public void bitred() {
    compile("bitred", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void compositionQuery() {
    compile("compositionQuery", TestSteps.EXECUTE, false, true);
  }

  @Test
  public void param() {
    compile("param", TestSteps.COMPILE_TO_LLVM, false, false);
  }

  @Test
  public void emptyElse() {
    compile("emptyElse", TestSteps.COMPILE_TO_LLVM, false, false);
  }

  @Test
  public void saturate() {
    compile("saturate", TestSteps.COMPILE_TO_LLVM, false, true);
  }

  @Test
  public void range2() {
    compile("range2", TestSteps.COMPILE_TO_LLVM, false, true);
  }

  @Test
  public void range3() {
    compile("range3", TestSteps.COMPILE_TO_LLVM, false, true);
  }

  @Test
  public void iffunc() {
    compile("iffunc", TestSteps.EXECUTE, false, true);
  }

  @Test
  public void convert1() {
    compile("convert1", TestSteps.EXECUTE, false, true);
  }

  @Test
  public void funcStruct() {
    compile("funcStruct", TestSteps.COMPILE_TO_ASM, false, true);
  }

  @Test
  public void linking() {
    compile("linking", "Linking", true, false, false);
  }

  @Test
  public void bool_Bool() {
    compile("bool", "Bool", true, false, false);
  }

  @Test
  public void bool2_Bool2() {
    compile("bool2", "Bool2", true, false, false);
  }

  @Test
  public void locvar() {
    compile("locvar", "Locvar", true, false, true);
  }

  // check:
  @Test
  public void expr_Expr() {
    compile("expr", "Expr", true, false, true);
  }

  @Test
  public void expr2_Expr2() {
    compile("expr2", "Expr2", true, false, false);
  }

  // @Test
  // public void compFib() {
  // compile("compFib", "CompFib", true, false, false);
  // }
}
