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

package ast.pass.reduction;

import java.util.HashMap;
import java.util.Map;

import main.Configuration;
import ast.Designator;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Is;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefFactory;
import ast.data.reference.RefName;
import ast.data.statement.CaseStmt;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.EnumTypeFactory;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.UnionType;
import ast.dispatcher.DfsTraverser;
import ast.dispatcher.other.ExprReplacer;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;
import ast.pass.AstPass;
import ast.repository.query.Match;
import ast.specification.HasName;

//TODO documentation
// DONE introduce enum E for union U
// DONE add element e of type E to U
// TODO replace access to union instance u in ".. is .." and "case" with access to u.e
// TODO replace also reference to element x of U to x' in E
// DONE replace "is" with "=="
public class ReduceUnion extends AstPass {
  public ReduceUnion(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    ReduceUnionWorker inst = new ReduceUnionWorker(kb);

    inst.traverse(ast, null);
    // FIXME only provide namespace as ast?
    ast.children.addAll(inst.getUnion2enum().values());
  }

}

class ReduceUnionWorker extends ExprReplacer<Void> {
  final private Map<UnionType, EnumType> union2enum = new HashMap<UnionType, EnumType>();
  final private KnowType kt;

  public ReduceUnionWorker(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  @Override
  public Expression traverse(Ast obj, Void param) {
    Uni2Enum enu2uni = new Uni2Enum();
    enu2uni.traverse(obj, getUnion2enum());
    return super.traverse(obj, param);
  }

  @Override
  protected Expression visitCaseStmt(CaseStmt obj, Void param) {
    Type et = kt.get(obj.condition);
    if (et instanceof UnionType) {
      assert (obj.condition instanceof ReferenceExpression);
      OffsetReference cond = (OffsetReference) ((ReferenceExpression) obj.condition).reference;
      cond.getOffset().add(new RefName(((UnionType) et).tag.getName()));
    }
    return super.visitCaseStmt(obj, param);
  }

  @Override
  protected Expression visitOffsetReference(OffsetReference obj, Void param) {
    super.visitOffsetReference(obj, param);
    LinkedAnchor anchor = (LinkedAnchor) obj.getAnchor();
    if (anchor.getLink() instanceof UnionType) {
      UnionType ut = (UnionType) anchor.getLink();
      assert (getUnion2enum().containsKey(ut));
      assert (obj.getOffset().size() == 1);
      assert (obj.getOffset().get(0) instanceof RefName);
      EnumType et = getUnion2enum().get(ut);
      String ev = ((RefName) obj.getOffset().get(0)).name;
      assert (Match.hasItem(et, new HasName(ev)));
      anchor.setLink(et);
      obj.getOffset().clear();
      obj.getOffset().add(new RefName(ev));
    }
    return null;
  }

  @Override
  protected Expression visitIs(Is obj, Void param) {
    super.visitIs(obj, param);
    OffsetReference left = (OffsetReference) ((ReferenceExpression) visit(obj.left, null)).reference;

    assert (left.getOffset().isEmpty());

    Type ut = kt.get(left);
    assert (ut instanceof UnionType);

    MetaList meta = left.metadata();
    left = RefFactory.create(left.getTarget(), new RefName(((UnionType) ut).tag.getName()));
    left.metadata().add(meta);

    ReferenceExpression leftRef = new ReferenceExpression(left);
    leftRef.metadata().add(meta);
    Equal equal = new Equal(leftRef, obj.right);
    equal.metadata().add(obj.metadata());
    return equal;
  }

  public Map<UnionType, EnumType> getUnion2enum() {
    return union2enum;
  }

}

class Uni2Enum extends DfsTraverser<Void, Map<UnionType, EnumType>> {
  private final static String ENUM_PREFIX = Designator.NAME_SEP + "enum";

  @Override
  protected Void visitUnionType(UnionType obj, Map<UnionType, EnumType> param) {
    assert (!param.containsKey(obj));

    EnumType et = EnumTypeFactory.create(ENUM_PREFIX + Designator.NAME_SEP + obj.getName());

    for (NamedElement elem : obj.element) {
      EnumElement ee = new EnumElement(elem.getName());
      et.element.add(ee);
    }

    obj.tag.typeref = TypeRefFactory.create(et);

    param.put(obj, et);
    return null;
  }
}
