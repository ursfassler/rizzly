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
import evl.data.Evl;
import evl.data.Namespace;
import evl.knowledge.KnowledgeBase;
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
import evl.pass.ForReduction;
import evl.pass.FuncInliner;
import evl.pass.HeaderWriter;
import evl.pass.IfCutter;
import evl.pass.InitVarTyper;
import evl.pass.IntroduceConvert;
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
import evl.pass.hfsmreduction.HfsmReduction;
import evl.pass.instantiation.Instantiation;
import evl.pass.modelcheck.Modelcheck;
import evl.pass.sanitycheck.Sanitycheck;
import evl.pass.typecheck.Typecheck;
import evl.traverser.other.ConstTyper;
import evl.traverser.other.SystemIfaceAdder;

public class MainEvl {
  public static void doEvl(ClaOption opt, String outdir, String debugdir, Namespace aclasses) {
    KnowledgeBase kb = new KnowledgeBase(aclasses, outdir, debugdir);
    PassGroup passes = evlPasses(opt);
    process(passes, new Designator(), new DebugPrinter(aclasses, kb.getDebugDir()), aclasses, kb);
  }

  // TODO split up
  private static PassGroup evlPasses(ClaOption opt) {
    PassGroup passes = new PassGroup("evl");

    passes.checks.add(new Sanitycheck());

    passes.add(new ConstTyper());

    if (!opt.doLazyModelCheck()) {
      // TODO check that model check does not change AST
      passes.add(new Modelcheck());
    }

    // FuncHeaderReplacegr.process(aclasses, kb); //TODO remove if not used

    passes.passes.add(reduce(passes));

    passes.add(new InitVarTyper());
    passes.add(new BitLogicCategorizer());
    passes.add(new Typecheck());
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

    passes.add(new Instantiation());

    passes.add(new HeaderWriter());

    passes.add(new ConstantPropagation());

    // Have to do it here since queue reduction creates enums
    passes.add(new EnumReduction());
    passes.add(new ConstantPropagation());

    passes.add(new RemoveUnused());

    passes.add(new IfCutter());

    passes.add(new RangeReplacer());
    passes.add(new RemoveUnused());

    passes.passes.add(optimize(passes));

    passes.passes.add(prepareForC(passes));

    passes.add(new CWriter());
    return passes;
  }

  private static PassGroup prepareForC(PassGroup passes) {
    PassGroup cprep = new PassGroup("cout");
    cprep.checks.addAll(passes.checks);

    cprep.add(new VarDeclToTop());
    cprep.add(new TypeMerge());
    cprep.add(new CRenamer());
    cprep.add(new TypeSort());
    cprep.add(new VarSort());
    return cprep;
  }

  private static PassGroup optimize(PassGroup passes) {
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
    return optimize;
  }

  // TODO separate things / better naming
  private static PassGroup reduce(PassGroup passes) {
    PassGroup reduction = new PassGroup("reduction");
    reduction.checks.addAll(passes.checks);
    reduction.add(new IntroduceConvert());
    reduction.add(new OpenReplace());
    reduction.add(new ElementaryReduction());
    reduction.add(new CompositionReduction());
    reduction.add(new HfsmReduction());
    reduction.add(new ReduceAliasType());
    reduction.add(new ReduceUnion());
    reduction.add(new ReduceTuple());
    return reduction;
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
  final public List<Pass> passes = new ArrayList<Pass>();
  final public List<EvlPass> checks = new ArrayList<EvlPass>();

  PassGroup(String name) {
    super(name);
  }

  public void add(EvlPass pass, String name) {
    passes.add(new PassItem(pass, name));
  }

  public void add(EvlPass pass) {
    passes.add(new PassItem(pass, pass.getClass().getSimpleName()));
  }

}
