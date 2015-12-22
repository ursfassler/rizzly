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

import org.junit.Ignore;
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
  public void templ1() {
    compile("templ1", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void defaultInit() {
    compile("defaultInit", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  public void tuple2() {
    compile("tuple2", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  public void mulret1() {
    compile("mulret1", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  public void mulret2() {
    compile("mulret2", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  @Ignore
  public void mulret3() {
    compile("mulret3", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  public void mulret4() {
    compile("mulret4", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  public void mulret5() {
    compile("mulret5", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  public void subrespLink() {
    compile("subrespLink", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  public void rec() {
    compile("rec", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void rec1() {
    compile("rec1", TestSteps.EXECUTE, false, false);
  }

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
  public void generic() {
    compile("generic", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void smallGenericUse() {
    compile("smallGenericUse", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void linkorder() {
    compile("linkorder", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  @Ignore
  public void debugMsg() {
    compile("debugMsg", TestSteps.EXECUTE, true, false);
  }

  @Test
  public void typefunc() {
    compile("typefunc", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void meta() {
    compile("meta", TestSteps.COMPILE_TO_AST, false, false);
  }

  @Test
  public void typeAlias1() {
    compile("typeAlias1", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void typeAlias2() {
    compile("typeAlias2", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void typeAlias3() {
    compile("typeAlias3", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void downCast() {
    compile("downCast", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void linkorder2() {
    compile("linkorder2", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void genIface() {
    compile("genIface", TestSteps.COMPILE_TESTCASE, false, true);
  }

  @Test
  public void alarmController() {
    compile("alarmController", TestSteps.COMPILE_TO_LIB, false, false);
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
  public void alarmClock() {
    compile("alarmClock", TestSteps.EXECUTE, false, false);
  }

  @Test
  public void union1() {
    compile("union1", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  @Ignore
  public void union2() {
    compile("union2", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  @Ignore
  public void union3() {
    compile("union3", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void union4() {
    compile("union4", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void unionTest() {
    compile("unionTest", TestSteps.COMPILE_TO_LIB, false, false);
  }

  @Test
  public void typedConst() {
    compile("typedConst", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  public void extConst() {
    compile("extConst", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  public void genericUse2() {
    compile("genericUse2", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  @Ignore
  public void genericUse() {
    compile("genericUse", TestSteps.COMPILE_TO_C, false, true);
  }

  @Test
  public void param() {
    compile("param", TestSteps.COMPILE_TO_C, false, false);
  }

  @Test
  public void funcStruct() {
    compile("funcStruct", TestSteps.COMPILE_TO_LIB, false, true);
  }

  @Test
  public void linking() {
    compile("linking", TestSteps.COMPILE_TO_LIB, false, false);
  }

}
