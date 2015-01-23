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

package fun.pass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import pass.FunPass;

import common.Designator;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.composition.ImplComposition;
import fun.expression.reference.BaseRef;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefName;
import fun.function.FuncHeader;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateContent;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.knowledge.KnowFunFile;
import fun.knowledge.KnowledgeBase;
import fun.other.CompImpl;
import fun.other.FunList;
import fun.other.ImplElementary;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.other.SymbolTable;
import fun.other.Template;
import fun.statement.Block;
import fun.statement.VarDefStmt;
import fun.traverser.TransitionStateLinker;
import fun.type.Type;
import fun.type.composed.RecordType;
import fun.type.template.Range;

public class Linker extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    LinkerWorker linker = new LinkerWorker(kb);
    linker.traverse(root, new SymbolTable());
  }

}

class LinkerWorker extends DefTraverser<Void, SymbolTable> {
  final private KnowFunFile kf;
  final private HashMap<State, SymbolTable> stateNames = new HashMap<State, SymbolTable>();

  public LinkerWorker(KnowledgeBase kb) {
    super();
    this.kf = kb.getEntry(KnowFunFile.class);
  }

  @Override
  protected Void visitNamespace(Namespace obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getChildren());
    return super.visitNamespace(obj, param);
  }

  @Override
  protected Void visitRizzlyFile(RizzlyFile obj, SymbolTable param) {
    SymbolTable pubs = new SymbolTable(param);
    SymbolTable rzys = new SymbolTable(pubs);
    SymbolTable locs = new SymbolTable(rzys);

    FunList<Named> objs = new FunList<Named>();
    for (Designator des : obj.getImports()) {
      RizzlyFile rzy = kf.get(des);
      assert (rzy != null);
      FunList<Named> named = rzy.getObjects().getItems(Named.class);
      objs.addAll(named);
      objs.add(rzy);
    }

    pubs.addAll(removeDuplicates(objs));

    locs.addAll(obj.getObjects());

    super.visitRizzlyFile(obj, locs);
    return null;
  }

  private FunList<Named> removeDuplicates(FunList<Named> objs) {
    Set<String> ambigous = new HashSet<String>();
    FunList<Named> map = new FunList<Named>();
    for (Named itr : objs) {
      if (!ambigous.contains(itr.getName())) {
        if (map.find(itr.getName()) != null) {
          map.remove(itr.getName());
          ambigous.add(itr.getName());
        } else {
          map.add(itr);
        }
      }
    }
    return map;
  }

  @Override
  protected Void visitDeclaration(Template obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getTempl());
    visitList(obj.getTempl(), param);
    visit(obj.getObject(), param);
    return null;
  }

  @Override
  protected Void visitRefName(RefName obj, SymbolTable param) {
    // TODO: this needs special linking and may not be possible from beginning (but after evaluation)
    return null;
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, SymbolTable param) {
    if (obj.getLink() instanceof DummyLinkTarget) {
      String name = ((DummyLinkTarget) obj.getLink()).getName();

      Named link = param.find(name);
      if (link == null) {
        RError.err(ErrorType.Error, obj.getInfo(), "Name not found: " + name);
      }
      assert (!(link instanceof DummyLinkTarget));

      obj.setLink(link);
    }
    return super.visitBaseRef(obj, param);
  }

  @Override
  protected Void visitComponent(CompImpl obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getObjects());
    super.visitComponent(obj, param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getConnection());
    param.addAll(obj.getInstantiation());
    super.visitImplComposition(obj, param);
    return null;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getDeclaration());
    param.addAll(obj.getInstantiation());
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);
    super.visitImplElementary(obj, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, SymbolTable param) {
    param = new SymbolTable(param);
    TransitionStateLinker.process(obj);

    param = new SymbolTable(param);
    visitList(obj.getInterface(), param);
    param.addAll(obj.getInterface());
    visit(obj.getTopstate(), param);

    return null;
  }

  @Override
  protected Void visitState(State obj, SymbolTable param) {
    param = new SymbolTable(param);

    param.addAll(obj.getItemList());

    assert (!stateNames.containsKey(obj));
    stateNames.put(obj, param);

    // visitList(obj.getItemList(), param);
    visit(obj.getEntryFunc(), param);
    visit(obj.getExitFunc(), param);

    super.visitState(obj, param);

    FunList<Transition> trans = obj.getItemList().getItems(Transition.class);
    FunList<StateContent> rest = new FunList<StateContent>(obj.getItemList());
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
    visit(obj.getInitial(), param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, SymbolTable param) {
    // visit(obj.getSrc(), param); // done by TransitionStateLinker
    // visit(obj.getDst(), param);
    visit(obj.getEvent(), param);
    visitList(obj.getParam(), param);

    param = new SymbolTable(param);
    param.addAll(obj.getParam());

    // get context from src state and add event arguments
    SymbolTable srcNames = stateNames.get(obj.getSrc().getLink());
    assert (srcNames != null);
    srcNames = new SymbolTable(srcNames);
    srcNames.addAll(obj.getParam());
    visit(obj.getGuard(), srcNames);

    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitFunctionHeader(FuncHeader obj, SymbolTable param) {
    param = new SymbolTable(param);
    param.addAll(obj.getParam());
    super.visitFunctionHeader(obj, param);
    return null;
  }

  @Override
  protected Void visitRecordType(RecordType obj, SymbolTable param) {
    param = new SymbolTable(param);
    super.visitRecordType(obj, param);
    return null;
  }

  @Override
  protected Void visitRange(Range obj, SymbolTable param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitType(Type obj, SymbolTable param) {
    return super.visitType(obj, new SymbolTable(param));
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, SymbolTable param) {
    param.add(obj.getVariable());
    super.visitVarDef(obj, param);
    return null;
  }

  @Override
  protected Void visitBlock(Block obj, SymbolTable param) {
    param = new SymbolTable(param);
    super.visitBlock(obj, param);
    return null;
  }

}
