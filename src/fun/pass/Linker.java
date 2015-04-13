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

import pass.EvlPass;

import common.Designator;

import error.ErrorType;
import error.RError;
import evl.data.EvlList;
import evl.data.Named;
import evl.data.Namespace;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateContent;
import evl.data.component.hfsm.StateSimple;
import evl.data.component.hfsm.Transition;
import evl.data.expression.reference.BaseRef;
import evl.data.expression.reference.DummyLinkTarget;
import evl.data.expression.reference.Reference;
import evl.data.file.RizzlyFile;
import evl.data.function.Function;
import evl.data.statement.Block;
import evl.data.statement.VarDefInitStmt;
import evl.data.type.Type;
import evl.data.type.base.RangeType;
import evl.knowledge.KnowFile;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;
import evl.traverser.other.ClassGetter;
import fun.other.RawComponent;
import fun.other.RawComposition;
import fun.other.RawElementary;
import fun.other.RawHfsm;
import fun.other.SymbolTable;
import fun.other.Template;
import fun.traverser.TransitionStateLinker;

public class Linker extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
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

    EvlList<Named> objs = new EvlList<Named>();
    for (Designator des : obj.getImports()) {
      RizzlyFile rzy = kf.get(des);
      assert (rzy != null);
      EvlList<Named> named = ClassGetter.filter(Named.class, rzy.getObjects());
      objs.addAll(named);
      objs.add(rzy);
    }

    pubs.addAll(removeDuplicates(objs));

    locs.addAll(obj.getObjects());

    super.visitRizzlyFile(obj, locs);
    return null;
  }

  private EvlList<Named> removeDuplicates(EvlList<Named> objs) {
    Set<String> ambigous = new HashSet<String>();
    EvlList<Named> map = new EvlList<Named>();
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
  protected Void visitRefName(evl.data.expression.reference.RefName obj, SymbolTable param) {
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

    EvlList<Transition> trans = ClassGetter.filter(Transition.class, obj.item);
    EvlList<StateContent> rest = new EvlList<StateContent>(obj.item);
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
  protected Void visitRecordType(evl.data.type.composed.RecordType obj, SymbolTable param) {
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
  protected Void visitForStmt(evl.data.statement.ForStmt obj, SymbolTable param) {
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
