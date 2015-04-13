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

import pass.AstPass;
import pass.Condition;
import ast.data.Ast;
import ast.data.Namespace;
import ast.knowledge.KnowledgeBase;
import ast.pass.AlwaysGreater;
import ast.pass.BitLogicCategorizer;
import ast.pass.BitnotFixer;
import ast.pass.BlockReduction;
import ast.pass.CRenamer;
import ast.pass.CWriter;
import ast.pass.CheckNames;
import ast.pass.CheckSimpleTypeRef;
import ast.pass.CompLinkReduction;
import ast.pass.CompareReplacer;
import ast.pass.CompositionReduction;
import ast.pass.ConstantPropagation;
import ast.pass.DebugIface;
import ast.pass.DocWriter;
import ast.pass.ElementaryReduction;
import ast.pass.EnumLinkReduction;
import ast.pass.EnumReduction;
import ast.pass.FileLoader;
import ast.pass.FileReduction;
import ast.pass.ForReduction;
import ast.pass.FuncInliner;
import ast.pass.HeaderWriter;
import ast.pass.IfCutter;
import ast.pass.InitVarTyper;
import ast.pass.InternFuncAdder;
import ast.pass.InternTypeAdder;
import ast.pass.IntroduceConvert;
import ast.pass.NamespaceLinkReduction;
import ast.pass.NoCallEmptyFunc;
import ast.pass.OpenReplace;
import ast.pass.RangeConverter;
import ast.pass.RangeReplacer;
import ast.pass.ReduceAliasType;
import ast.pass.ReduceMultiAssignment;
import ast.pass.ReduceRawComp;
import ast.pass.ReduceTuple;
import ast.pass.ReduceUnion;
import ast.pass.ReduceVarDefInit;
import ast.pass.RemoveUnused;
import ast.pass.RetStructIntroducer;
import ast.pass.RootInstanceAdder;
import ast.pass.SimplifyRef;
import ast.pass.StateLinkReduction;
import ast.pass.StateVarInitExecutor;
import ast.pass.TautoExprDel;
import ast.pass.TautoStmtDel;
import ast.pass.TupleAssignReduction;
import ast.pass.TypeMerge;
import ast.pass.TypeSort;
import ast.pass.TypeUplift;
import ast.pass.UnusedRemover;
import ast.pass.VarDeclToTop;
import ast.pass.VarDefSplitter;
import ast.pass.VarSort;
import ast.pass.hfsmreduction.HfsmReduction;
import ast.pass.instantiation.Instantiation;
import ast.pass.linker.Linker;
import ast.pass.modelcheck.Modelcheck;
import ast.pass.sanitycheck.Sanitycheck;
import ast.pass.specializer.TypeEvalReplacerPass;
import ast.pass.typecheck.Typecheck;
import ast.traverser.other.ConstTyper;
import ast.traverser.other.SystemIfaceAdder;

import common.Designator;
import common.ElementInfo;

import debug.DebugPrinter;
import error.RError;

public class Passes {
  public static void process(ClaOption opt, String outdir, String debugdir) {
    Namespace aclasses = new Namespace(ElementInfo.NO, "!");
    KnowledgeBase kb = new KnowledgeBase(aclasses, outdir, debugdir, opt);
    PassGroup passes = makePasses(opt);
    process(passes, new Designator(), new DebugPrinter(aclasses, kb.getDebugDir()), aclasses, kb);
  }

  // TODO split up
  private static PassGroup makePasses(ClaOption opt) {
    PassGroup passes = new PassGroup("ast");

    passes.checks.add(new Sanitycheck());

    passes.passes.add(funPasses(opt));

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

  private static PassGroup funPasses(ClaOption opt) {
    PassGroup passes = new PassGroup("fun");

    passes.add(new FileLoader());

    passes.add(new InternTypeAdder());
    passes.add(new InternFuncAdder());

    passes.add(new CheckNames());
    passes.add(new Linker());
    if (opt.getDocOutput()) {
      passes.add(new DocWriter());
    }
    passes.add(new FileReduction());

    passes.add(new NamespaceLinkReduction());
    passes.add(new StateLinkReduction());
    passes.add(new EnumLinkReduction());
    passes.add(new CompLinkReduction());

    passes.add(new RootInstanceAdder());

    passes.add(new TypeEvalReplacerPass());

    passes.add(new VarDefSplitter());
    passes.add(new UnusedRemover());
    passes.add(new StateVarInitExecutor());
    passes.add(new UnusedRemover());

    // what was in FunToEvl
    passes.add(new ReduceMultiAssignment());
    passes.add(new ReduceRawComp());
    passes.add(new SimplifyRef());
    passes.add(new CheckSimpleTypeRef());
    passes.add(new ReduceVarDefInit());

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

  public static void process(PassGroup group, Designator prefix, DebugPrinter dp, Namespace ast, KnowledgeBase kb) {
    prefix = new Designator(prefix, group.getName());
    for (Pass pass : group.passes) {
      if (pass instanceof PassGroup) {
        process((PassGroup) pass, prefix, dp, ast, kb);
      } else if (pass instanceof PassItem) {
        process((PassItem) pass, prefix, dp, ast, kb);
      } else {
        throw new RuntimeException("not yet implemented: " + pass.getClass().getCanonicalName());
      }
      for (AstPass check : group.checks) {
        check.process(ast, kb);
      }
    }
  }

  public static void process(PassItem item, Designator prefix, DebugPrinter dp, Namespace ast, KnowledgeBase kb) {
    for (Condition cond : item.pass.getPrecondition()) {
      RError.ass(cond.check(ast, kb), item.getName() + ": precondition does not hold: " + cond.getName());
    }

    prefix = new Designator(prefix, item.getName());
    item.pass.process(ast, kb);
    dp.print(prefix.toString("."));

    for (Condition cond : item.pass.getPostcondition()) {
      RError.ass(cond.check(ast, kb), item.getName() + ": postcondition does not hold: " + cond.getName());
    }
  }

  @SuppressWarnings("unused")
  private static void print(Map<Ast, Boolean> writes, Map<Ast, Boolean> reads, Map<Ast, Boolean> outputs, Map<Ast, Boolean> inputs) {
    for (Ast header : writes.keySet()) {
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
  AstPass pass;

  public PassItem(AstPass pass, String name) {
    super(name);
    this.pass = pass;
  }
}

class PassGroup extends Pass {
  final public List<Pass> passes = new ArrayList<Pass>();
  final public List<AstPass> checks = new ArrayList<AstPass>();

  PassGroup(String name) {
    super(name);
  }

  public void add(AstPass pass, String name) {
    passes.add(new PassItem(pass, name));
  }

  public void add(AstPass pass) {
    passes.add(new PassItem(pass, pass.getClass().getSimpleName()));
  }

}
