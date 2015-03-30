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

package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pass.Condition;
import pass.EvlPass;

import common.Designator;

import debug.DebugPrinter;
import error.RError;
import evl.Evl;
import evl.hfsm.reduction.EntryExitUpdater;
import evl.hfsm.reduction.FsmReduction;
import evl.hfsm.reduction.LeafStateUplifter;
import evl.hfsm.reduction.QueryDownPropagator;
import evl.hfsm.reduction.StateItemUplifter;
import evl.hfsm.reduction.StateVarReplacer;
import evl.hfsm.reduction.TransitionDownPropagator;
import evl.hfsm.reduction.TransitionRedirecter;
import evl.hfsm.reduction.TransitionUplifter;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.pass.AlwaysGreater;
import evl.pass.BitLogicCategorizer;
import evl.pass.BitnotFixer;
import evl.pass.BlockReduction;
import evl.pass.CRenamer;
import evl.pass.CWriter;
import evl.pass.CompareReplacer;
import evl.pass.CompositionReduction;
import evl.pass.ConstantPropagation;
import evl.pass.DebugIface;
import evl.pass.ElementaryReduction;
import evl.pass.EnumReduction;
import evl.pass.Flattner;
import evl.pass.ForReduction;
import evl.pass.FuncInliner;
import evl.pass.HeaderWriter;
import evl.pass.IfCutter;
import evl.pass.InitVarTyper;
import evl.pass.Instantiation;
import evl.pass.IntroduceConvert;
import evl.pass.LinkReduction;
import evl.pass.NoCallEmptyFunc;
import evl.pass.OpenReplace;
import evl.pass.RangeConverter;
import evl.pass.RangeReplacer;
import evl.pass.ReduceAliasType;
import evl.pass.ReduceTuple;
import evl.pass.ReduceUnion;
import evl.pass.RemoveUnused;
import evl.pass.RetStructIntroducer;
import evl.pass.TautoExprDel;
import evl.pass.TautoStmtDel;
import evl.pass.TupleAssignReduction;
import evl.pass.TypeMerge;
import evl.pass.TypeSort;
import evl.pass.TypeUplift;
import evl.pass.VarDeclToTop;
import evl.pass.VarSort;
import evl.pass.check.CompInterfaceTypeChecker;
import evl.pass.check.HfsmTransScopeCheck;
import evl.pass.check.Io;
import evl.pass.check.ModelChecker;
import evl.pass.check.Root;
import evl.pass.check.RtcViolation;
import evl.pass.check.Usefullness;
import evl.pass.check.type.TypeChecker;
import evl.pass.infrastructure.LinkTargetExists;
import evl.pass.infrastructure.SingleDefinition;
import evl.pass.infrastructure.VarLinkOk;
import evl.queue.QueueReduction;
import evl.traverser.ConstTyper;
import evl.traverser.SystemIfaceAdder;

//TODO ensure that composition and hfsm use construct and destruct correctly

public class MainEvl {
  public static void doEvl(ClaOption opt, String outdir, String debugdir, Namespace aclasses) {
    KnowledgeBase kb = new KnowledgeBase(aclasses, outdir, debugdir);

    PassGroup passes = new PassGroup("evl");

    passes.addCheck(new LinkTargetExists());
    passes.addCheck(new VarLinkOk());
    passes.addCheck(new SingleDefinition());
    // passes.addCheck(TypeChecker.class);

    passes.add(new ConstTyper());

    if (!opt.doLazyModelCheck()) {
      // TODO check that model check does not change AST
      PassGroup modelcheck = new PassGroup("modelcheck");
      modelcheck.add(new Root());
      modelcheck.add(new Usefullness());
      modelcheck.add(new RtcViolation());
      modelcheck.add(new Io());
      modelcheck.add(new HfsmTransScopeCheck());
      modelcheck.add(new CompInterfaceTypeChecker());
      modelcheck.add(new ModelChecker());
      passes.add(modelcheck);
    }

    // FuncHeaderReplacegr.process(aclasses, kb); //TODO remove if not used

    {
      PassGroup reduction = new PassGroup("reduction");
      reduction.checks.addAll(passes.checks);
      reduction.add(new IntroduceConvert());
      reduction.add(new OpenReplace());
      reduction.add(new ElementaryReduction());
      reduction.add(new CompositionReduction());
      {
        PassGroup hfsm = new PassGroup("hfsm");
        hfsm.add(new QueryDownPropagator());
        hfsm.add(new TransitionRedirecter());
        hfsm.add(new TransitionDownPropagator());
        hfsm.add(new StateVarReplacer());
        hfsm.add(new EntryExitUpdater());
        hfsm.add(new StateItemUplifter());
        hfsm.add(new TransitionUplifter());
        hfsm.add(new LeafStateUplifter());
        hfsm.add(new FsmReduction());
        reduction.add(hfsm);
      }
      reduction.add(new ReduceAliasType());
      reduction.add(new ReduceUnion());
      reduction.add(new ReduceTuple());
      passes.add(reduction);
    }

    passes.add(new InitVarTyper());
    passes.add(new BitLogicCategorizer());
    passes.add(new TypeChecker());
    passes.add(new SystemIfaceAdder());

    passes.add(new RetStructIntroducer());
    passes.add(new TupleAssignReduction());
    // passes.add(new ExprCutter());

    if (opt.doDebugEvent()) {
      passes.add(new DebugIface());
      // only for debugging
      // passes.add(TypeChecker.class);
    }

    passes.add(new AlwaysGreater());

    passes.add(new ForReduction());

    passes.add(new TypeUplift());
    passes.add(new CompareReplacer());
    passes.add(new RangeConverter());

    passes.add(new BitnotFixer());

    {
      PassGroup inst = new PassGroup("instantiation");
      // inst.checks.addAll(passes.checks);
      inst.add(new Instantiation());
      inst.add(new LinkReduction());
      inst.add(new QueueReduction());
      inst.add(new Flattner());
      inst.add(new RemoveUnused());
      passes.add(inst);
    }

    passes.add(new HeaderWriter());

    passes.add(new ConstantPropagation());

    // Have to do it here since queue reduction creates enums
    passes.add(new EnumReduction());
    passes.add(new ConstantPropagation());

    passes.add(new RemoveUnused());

    passes.add(new IfCutter());

    passes.add(new RangeReplacer());
    passes.add(new RemoveUnused());

    {
      PassGroup optimize = new PassGroup("optimize");
      optimize.checks.addAll(passes.checks);

      optimize.add(new BlockReduction());
      optimize.add(new NoCallEmptyFunc());
      optimize.add(new RemoveUnused());
      optimize.add(new FuncInliner());
      optimize.add(new TautoExprDel());
      optimize.add(new TautoStmtDel());
      optimize.add(new RemoveUnused());
      optimize.add(new BlockReduction());
      passes.add(optimize);
    }

    {
      PassGroup cprep = new PassGroup("cout");
      cprep.checks.addAll(passes.checks);

      cprep.add(new VarDeclToTop());
      cprep.add(new TypeMerge());
      cprep.add(new CRenamer());
      cprep.add(new TypeSort());
      cprep.add(new VarSort());
      cprep.add(new CWriter());
      passes.add(cprep);
    }

    process(passes, new Designator(), new DebugPrinter(aclasses, kb.getDebugDir()), aclasses, kb);
  }

  public static void process(PassGroup group, Designator prefix, DebugPrinter dp, Namespace evl, KnowledgeBase kb) {
    prefix = new Designator(prefix, group.getName());
    for (Pass pass : group.passes) {
      if (pass instanceof PassGroup) {
        process((PassGroup) pass, prefix, dp, evl, kb);
      } else if (pass instanceof PassItem) {
        process((PassItem) pass, prefix, dp, evl, kb);
      } else {
        throw new RuntimeException("not yet implemented: " + pass.getClass().getCanonicalName());
      }
      for (EvlPass check : group.checks) {
        check.process(evl, kb);
      }
    }
  }

  public static void process(PassItem item, Designator prefix, DebugPrinter dp, Namespace evl, KnowledgeBase kb) {
    for (Condition cond : item.pass.getPrecondition()) {
      RError.ass(cond.check(evl, kb), item.getName() + ": precondition does not hold: " + cond.getName());
    }

    prefix = new Designator(prefix, item.getName());
    item.pass.process(evl, kb);
    dp.print(prefix.toString("."));

    for (Condition cond : item.pass.getPostcondition()) {
      RError.ass(cond.check(evl, kb), item.getName() + ": postcondition does not hold: " + cond.getName());
    }
  }

  @SuppressWarnings("unused")
  private static void print(Map<Evl, Boolean> writes, Map<Evl, Boolean> reads, Map<Evl, Boolean> outputs, Map<Evl, Boolean> inputs) {
    for (Evl header : writes.keySet()) {
      String rwio = "";
      rwio += reads.get(header) ? "r" : " ";
      rwio += writes.get(header) ? "w" : " ";
      rwio += inputs.get(header) ? "i" : " ";
      rwio += outputs.get(header) ? "o" : " ";
      System.out.print(rwio);
      System.out.print("\t");
      System.out.print(header);
      System.out.println();
    }
  }

}

class Pass {
  final private String name;

  Pass(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

}

class PassItem extends Pass {
  EvlPass pass;

  public PassItem(EvlPass pass, String name) {
    super(name);
    this.pass = pass;
  }
}

class PassGroup extends Pass {
  List<Pass> passes = new ArrayList<Pass>();
  List<EvlPass> checks = new ArrayList<EvlPass>();

  PassGroup(String name) {
    super(name);
  }

  public void add(EvlPass pass, String name) {
    add(new PassItem(pass, name));
  }

  public void add(EvlPass pass) {
    add(new PassItem(pass, pass.getClass().getSimpleName()));
  }

  public void add(Pass pass) {
    passes.add(pass);
  }

  public void addCheck(EvlPass pass) {
    checks.add(pass);
  }

}
