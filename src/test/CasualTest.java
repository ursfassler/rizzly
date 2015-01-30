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
  public void ctfeCast() {
    compile("ctfeCast", TestSteps.COMPILE_TO_LLVM, false, false);
  }

  @Test
  public void subrespLink() {
    compile("subrespLink", TestSteps.COMPILE_TO_LLVM, false, false);
  }

  @Test
  public void logicNot() {
    compile("logicNot", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void bitNot() {
    compile("bitNot", TestSteps.EXECUTE, false, false);
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
  @Test
  public void rec2() {
    compile("rec2", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void rec3() {
    compile("rec3", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void rec4() {
    compile("rec4", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void while_() {
    compile("while", TestSteps.EXECUTE, false, true);
  }

  @Test
  public void range() {
    compile("range", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void generic() {
    compile("generic", TestSteps.COMPILE_TO_ASM, false, false);
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
  public void composition2() {
    compile("composition2", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void composition1() {
    compile("composition1", TestSteps.EXECUTE, true, false);
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
  public void typeAlias1() {
    compile("typeAlias1", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void typeAlias2() {
    compile("typeAlias2", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void typeAlias3() {
    compile("typeAlias3", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void downCast() {
    compile("downCast", TestSteps.EXECUTE, false, false);
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
  public void constInit() {
    compile("constInit", TestSteps.COMPILE_TO_LLVM, false, false);
  }

  @Test
  public void constfunc() {
    compile("constfunc", TestSteps.COMPILE_TO_LLVM, false, false);
  }

  @Test
  public void constInit2() {
    compile("constInit2", TestSteps.COMPILE_TO_LLVM, false, false);
  }

  @Test
  public void constInit3() {
    compile("constInit3", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void constInit4() {
    compile("constInit4", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void constInit5() {
    compile("constInit5", TestSteps.COMPILE_TO_ASM, false, false);
  }

  // TODO add test case
  @Test
  public void constInit6() {
    compile("constInit6", TestSteps.COMPILE_TO_ASM, false, false);
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
  public void union2() {
    compile("union2", TestSteps.COMPILE_TO_ASM, false, false);// TODO make testcase
  }

  @Test
  public void union3() {
    compile("union3", TestSteps.COMPILE_TO_ASM, false, false);// TODO make testcase
  }

  @Test
  public void union4() {
    compile("union4", TestSteps.COMPILE_TO_ASM, false, false);// TODO make testcase
  }

  @Test
  public void unionTest() {
    compile("unionTest", TestSteps.COMPILE_TO_ASM, false, false); // TODO make testcase
  }

  @Test
  public void typedConst() {
    compile("typedConst", TestSteps.COMPILE_TO_PIR, false, false);
  }

  @Test
  public void constdef() {
    compile("constdef", TestSteps.COMPILE_TO_PIR, false, false);
  }

  @Test
  public void constdef2() {
    compile("constdef2", TestSteps.COMPILE_TO_PIR, false, false);
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
  public void boolext() {
    compile("boolext", TestSteps.COMPILE_TO_ASM, false, false);
  }

  @Test
  public void array1() {
    compile("array1", TestSteps.COMPILE_TO_ASM, false, false);
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
