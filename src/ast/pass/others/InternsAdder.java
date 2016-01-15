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

package ast.pass.others;

import main.Configuration;
import ast.data.function.template.DefaultValueTemplateFactory;
import ast.data.type.base.BooleanType;
import ast.data.type.base.StringType;
import ast.data.type.special.AnyType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.data.type.special.TypeType;
import ast.data.type.special.TypeTypeFactory;
import ast.data.type.special.VoidType;
import ast.data.type.template.ArrayTemplateFactory;
import ast.data.type.template.RangeTemplateFactory;
import ast.data.type.template.TypeTypeTemplateFactory;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

public class InternsAdder extends AstPass {
  public InternsAdder(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    IntegerType intType = new IntegerType();
    AnyType anyType = new AnyType();
    TypeType typeTypeAny = TypeTypeFactory.create(anyType);

    root.children.add(intType);
    root.children.add(anyType);
    root.children.add(typeTypeAny);

    root.children.add(new BooleanType());
    root.children.add(new VoidType());
    root.children.add(new NaturalType());
    root.children.add(new StringType());

    root.children.add(RangeTemplateFactory.create(intType));
    root.children.add(ArrayTemplateFactory.create(intType, typeTypeAny));
    root.children.add(TypeTypeTemplateFactory.create(typeTypeAny));

    root.children.add(DefaultValueTemplateFactory.create(typeTypeAny));
  }

}
