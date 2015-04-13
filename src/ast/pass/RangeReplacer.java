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

package ast.pass;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import pass.AstPass;
import ast.copy.Relinker;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.type.Type;
import ast.data.type.base.RangeType;
import ast.data.type.out.IntType;
import ast.data.type.out.SIntType;
import ast.data.type.out.UIntType;
import ast.knowledge.KnowledgeBase;
import ast.pass.typecheck.ExpressionTypecheck;
import ast.traverser.NullTraverser;
import ast.traverser.other.ClassGetter;

import common.ElementInfo;

import error.ErrorType;
import error.RError;

/**
 * Replaces range types with integer types
 *
 * @author urs
 *
 */
public class RangeReplacer extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {

    RangeReplacerWorker changer = new RangeReplacerWorker();
    for (Type old : ClassGetter.getRecursive(Type.class, ast)) {
      changer.traverse(old, null);
    }

    ast.children.addAll(changer.getSigned().values());
    ast.children.addAll(changer.getUnsigned().values());

    Relinker.relink(ast, changer.getMap());
  }

}

class RangeReplacerWorker extends NullTraverser<Void, Void> {
  private final Map<Integer, SIntType> signed = new HashMap<Integer, SIntType>();
  private final Map<Integer, UIntType> unsigned = new HashMap<Integer, UIntType>();
  private final Map<RangeType, Type> map = new HashMap<RangeType, Type>();
  private final int allowedByteSizes[] = { 1, 2, 4, 8 }; // TODO make a
                                                         // parameter (is
                                                         // probably target
                                                         // specific)

  public Map<Integer, SIntType> getSigned() {
    return signed;
  }

  public Map<Integer, UIntType> getUnsigned() {
    return unsigned;
  }

  public Map<RangeType, Type> getMap() {
    return map;
  }

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    return null;
  }

  @Override
  protected Void visitUIntType(UIntType obj, Void param) {
    throw new RuntimeException("should not exist");
  }

  @Override
  protected Void visitSIntType(SIntType obj, Void param) {
    throw new RuntimeException("should not exist");
  }

  @Override
  protected Void visitRangeType(RangeType obj, Void param) {
    // TODO implement also for signed
    // TODO add range offset movement (i.e. move R{10,20} to R{0,10})
    BigInteger low = obj.range.low;
    boolean hasNeg = low.compareTo(BigInteger.ZERO) < 0; // TODO ok?
    if (hasNeg) {
      low = low.add(BigInteger.ONE).abs();
    }
    BigInteger max = low.max(obj.range.high);
    int bits = ExpressionTypecheck.bitCount(max);
    if (hasNeg) {
      bits++;
    }

    int bytes = (bits + 7) / 8;

    if (bytes > allowedByteSizes[allowedByteSizes.length - 1]) {
      RError.err(ErrorType.Fatal, "Found type with too many bits: " + obj.toString());
    }

    for (int i = 0; i < allowedByteSizes.length; i++) {
      if (bytes <= allowedByteSizes[i]) {
        bytes = allowedByteSizes[i];
        break;
      }
    }

    IntType ret;
    if (hasNeg) {
      ret = getSint(bytes);
    } else {
      ret = getUint(bytes);
    }
    map.put(obj, ret);

    return null;
  }

  private UIntType getUint(int bytes) {
    UIntType ret = unsigned.get(bytes);
    if (ret == null) {
      ret = new UIntType(ElementInfo.NO, UIntType.makeName(bytes), bytes);
      unsigned.put(bytes, ret);
    }
    assert (ret != null);
    return ret;
  }

  private SIntType getSint(int bytes) {
    SIntType ret = signed.get(bytes);
    if (ret == null) {
      ret = new SIntType(ElementInfo.NO, SIntType.makeName(bytes), bytes);
      signed.put(bytes, ret);
    }
    assert (ret != null);
    return ret;
  }
}
