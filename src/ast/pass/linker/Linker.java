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

package ast.pass.linker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ast.Designator;
import ast.data.AstList;
import ast.data.Named;
import ast.data.Namespace;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateContent;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.reference.BaseRef;
import ast.data.expression.reference.DummyLinkTarget;
import ast.data.expression.reference.Reference;
import ast.data.file.RizzlyFile;
import ast.data.function.Function;
import ast.data.raw.RawComponent;
import ast.data.raw.RawComposition;
import ast.data.raw.RawElementary;
import ast.data.raw.RawHfsm;
import ast.data.statement.Block;
import ast.data.statement.VarDefInitStmt;
import ast.data.template.Template;
import ast.data.type.Type;
import ast.data.type.base.RangeType;
import ast.knowledge.KnowFile;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.specification.TypeFilter;
import ast.traverser.DefTraverser;
import error.ErrorType;
import error.RError;

public class Linker extends AstPass {

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    LinkerWorker linker = new LinkerWorker(kb);
    linker.traverse(root, new SymbolTable());
  }

}

class LinkerWorker extends DefTraverser<Void, SymbolTable> {
  final private KnowFile kf;
  final private HashMap<State, SymbolTable> stateNames = new HashMap<State, SymbolTable>();

  public LinkerWorker(KnowledgeBase kb) {
    super();
    this.kf = kb.getEntry(KnowFile.class);
  }

  @Override
  protected Void visitNamespace(Namespace obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.children);
    return super.visitNamespace(obj, param);
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, SymbolTable param) {
    SymbolTable pubs = new SymbolTable(param);
    SymbolTable rzys = new SymbolTable(pubs);
    SymbolTable locs = new SymbolTable(rzys);

    AstList<Named> objs = new AstList<Named>();
    for (Designator des : obj.getImports()) {
      RizzlyFile rzy = kf.get(des);
      assert (rzy != null);
      AstList<Named> named = TypeFilter.select(rzy.getObjects(), Named.class);
      objs.addAll(named);
      objs.add(rzy);
    }

    pubs.addAll(removeDuplicates(objs));

    locs.addAll(obj.getObjects());

    super.visitRizzlyFile(obj, locs);
    return null;
  }

  private AstList<Named> removeDuplicates(AstList<Named> objs) {
    Set<String> ambigous = new HashSet<String>();
    AstList<Named> map = new AstList<Named>();
    for (Named itr : objs) {
      if (!ambigous.contains(itr.name)) {
        if (map.findFirst(itr.name) != null) {
          map.remove(itr.name);
          ambigous.add(itr.name);
        } else {
          map.add(itr);
        }
      }
    }
    return map;
  }

  @Override
  protected Void visitTemplate(Template obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getTempl());
    visitList(obj.getTempl(), param);
    visit(obj.getObject(), param);
    return null;
  }

  @Override
  protected Void visitRefName(ast.data.expression.reference.RefName obj, SymbolTable param) {
    // TODO: this needs special linking and may not be possible from beginning
    // (but after evaluation)
    return null;
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, SymbolTable param) {
    if (obj.link instanceof DummyLinkTarget) {
      String name = ((DummyLinkTarget) obj.link).name;

      Named link = param.find(name);
      if (link == null) {
        RError.err(ErrorType.Error, obj.getInfo(), "Name not found: " + name);
      }
      assert (!(link instanceof DummyLinkTarget));

      obj.link = link;
    }
    return super.visitBaseRef(obj, param);
  }

  @Override
  protected Void visitRawComponent(RawComponent obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getIface());
    super.visitRawComponent(obj, param);
    return null;
  }

  @Override
  protected Void visitRawComposition(RawComposition obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getConnection());
    param.addAll(obj.getInstantiation());
    super.visitRawComposition(obj, param);
    return null;
  }

  @Override
  protected Void visitRawElementary(RawElementary obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getDeclaration());
    param.addAll(obj.getInstantiation());
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    super.visitRawElementary(obj, param);
    return null;
  }

  @Override
  protected Void visitRawHfsm(RawHfsm obj, SymbolTable param) {
    param = new SymbolTable(param);
    TransitionStateLinker.process(obj);

    param = new SymbolTable(param);
    visitList(obj.getIface(), param);
    param.addAll(obj.getIface());
    visit(obj.getTopstate(), param);

    return null;
  }

  @Override
  protected Void visitState(State obj, SymbolTable param) {
    param = new SymbolTable(param);

    param.addAll(obj.item);

    assert (!stateNames.containsKey(obj));
    stateNames.put(obj, param);

    // visitList(obj.getItemList(), param);
    visit(obj.entryFunc.link.body, param);
    visit(obj.exitFunc.link.body, param);

    super.visitState(obj, param);

    AstList<Transition> trans = TypeFilter.select(obj.item, Transition.class);
    AstList<StateContent> rest = new AstList<StateContent>(obj.item);
    rest.removeAll(trans);
    visitList(rest, param);
    visitList(trans, param);

    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, SymbolTable param) {
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, SymbolTable param) {
    visit(obj.initial, param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, SymbolTable param) {
    // visit(obj.src, param); // done by TransitionStateLinker
    // visit(obj.dst, param);
    visit(obj.eventFunc, param);
    visitList(obj.param, param);

    param = new SymbolTable(param);
    param.addAll(obj.param);

    // get context from src state and add event arguments
    SymbolTable srcNames = stateNames.get(((Reference) obj.src).link);
    assert (srcNames != null);
    srcNames = new SymbolTable(srcNames);
    srcNames.addAll(obj.param);
    visit(obj.guard, srcNames);

    visit(obj.body, param);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.param);
    super.visitFunction(obj, param);
    return null;
  }

  @Override
  protected Void visitRecordType(ast.data.type.composed.RecordType obj, SymbolTable param) {
    param = new SymbolTable(param);
    super.visitRecordType(obj, param);
    return null;
  }

  @Override
  protected Void visitRangeType(RangeType obj, SymbolTable param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitType(Type obj, SymbolTable param) {
    return super.visitType(obj, new SymbolTable(param));
  }

  @Override
  protected Void visitVarDefInitStmt(VarDefInitStmt obj, SymbolTable param) {
    param.addAll(obj.variable);
    super.visitVarDefInitStmt(obj, param);
    return null;
  }

  @Override
  protected Void visitForStmt(ast.data.statement.ForStmt obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.add(obj.iterator);
    super.visitForStmt(obj, param);
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, SymbolTable param) {
    param = new SymbolTable(param);
    super.visitBlock(obj, param);
    return null;
  }

}
