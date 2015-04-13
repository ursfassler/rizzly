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

package fun.traverser.spezializer;

import java.math.BigInteger;
import java.util.List;

import util.Range;
import evl.data.Evl;
import evl.data.expression.Number;
import evl.data.type.Type;
import evl.data.type.special.AnyType;
import evl.data.type.special.IntegerType;
import evl.data.type.special.NaturalType;
import evl.data.type.template.ArrayTemplate;
import evl.data.type.template.RangeTemplate;
import evl.data.type.template.TypeTemplate;
import evl.data.type.template.TypeTypeTemplate;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowInstance;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import fun.other.ActualTemplateArgument;

public class TypeTemplateSpecializer extends NullTraverser<Type, List<ActualTemplateArgument>> {
  private final KnowledgeBase kb;
  private final KnowInstance ki;
  private final KnowBaseItem kbi;

  public TypeTemplateSpecializer(KnowledgeBase kb) {
    this.kb = kb;
    ki = kb.getEntry(KnowInstance.class);
    kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static Type process(TypeTemplate type, List<ActualTemplateArgument> genspec, KnowledgeBase kb) {
    TypeTemplateSpecializer specializer = new TypeTemplateSpecializer(kb);
    return specializer.traverse(type, genspec);
  }

  @Override
  protected evl.data.type.Type visitDefault(Evl obj, List<ActualTemplateArgument> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected evl.data.type.Type visitRangeTemplate(RangeTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 2);
    evl.data.type.Type ret = (evl.data.type.Type) ki.find(obj, param);
    if (ret == null) {
      ActualTemplateArgument low = ArgEvaluator.process(new IntegerType(), param.get(0), kb);
      assert (low instanceof Number);
      ActualTemplateArgument high = ArgEvaluator.process(new IntegerType(), param.get(1), kb);
      assert (high instanceof Number);
      ret = kbi.getRangeType(new Range(((Number) low).value, ((Number) high).value));
      ki.add(obj, param, ret);
    }
    return ret;
  }

  @Override
  protected evl.data.type.Type visitArrayTemplate(ArrayTemplate obj, List<ActualTemplateArgument> param) {
    assert (param.size() == 2);
    evl.data.type.Type ret = (evl.data.type.Type) ki.find(obj, param);
    if (ret == null) {
      ActualTemplateArgument size = ArgEvaluator.process(new NaturalType(), param.get(0), kb);
      ActualTemplateArgument type = ArgEvaluator.process(new AnyType(), param.get(1), kb);
      assert (type instanceof Type);
      assert (size instanceof Number);
      BigInteger count = ((evl.data.expression.Number) size).value;
      ret = kbi.getArray(count, (evl.data.type.Type) type);
      ki.add(obj, param, ret);
    }
    return ret;
  }

  @Override
  protected evl.data.type.Type visitTypeTypeTemplate(TypeTypeTemplate obj, List<ActualTemplateArgument> param) {
    evl.data.type.Type ret = (evl.data.type.Type) ki.find(obj, param);
    if (ret == null) {
      assert (param.size() == 1);
      ActualTemplateArgument type = ArgEvaluator.process(new AnyType(), param.get(0), kb);
      assert (type instanceof Type);
      ret = kbi.getTypeType((evl.data.type.Type) type);
      ki.add(obj, param, ret);
    }
    return ret;
  }
}
