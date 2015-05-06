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

import ast.Designator;
import ast.ElementInfo;
import ast.data.Ast;
import ast.data.Namespace;
import ast.debug.DebugPrinter;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.pass.check.model.Modelcheck;
import ast.pass.check.sanity.Sanitycheck;
import ast.pass.check.type.Typecheck;
import ast.pass.eval.GlobalConstEval;
import ast.pass.eval.TemplateArgEval;
import ast.pass.instantiation.Instantiation;
import ast.pass.linker.Linker;
import ast.pass.optimize.AlwaysGreater;
import ast.pass.optimize.BlockReduction;
import ast.pass.optimize.FuncInliner;
import ast.pass.optimize.NoCallEmptyFunc;
import ast.pass.optimize.RemoveUnused;
import ast.pass.optimize.TautoExprDel;
import ast.pass.optimize.TautoStmtDel;
import ast.pass.optimize.UnusedRemover;
import ast.pass.others.BitnotFixer;
import ast.pass.others.CRenamer;
import ast.pass.others.CWriter;
import ast.pass.others.CheckNames;
import ast.pass.others.CheckSimpleTypeRef;
import ast.pass.others.CompareReplacer;
import ast.pass.others.ConstTyper;
import ast.pass.others.ConstantPropagation;
import ast.pass.others.DebugIface;
import ast.pass.others.DocWriter;
import ast.pass.others.FileLoader;
import ast.pass.others.HeaderWriter;
import ast.pass.others.IfCutter;
import ast.pass.others.InitVarTyper;
import ast.pass.others.InternsAdder;
import ast.pass.others.IntroduceConvert;
import ast.pass.others.RangeConverter;
import ast.pass.others.RetStructIntroducer;
import ast.pass.others.RootInstanceAdder;
import ast.pass.others.SystemIfaceAdder;
import ast.pass.others.TypeMerge;
import ast.pass.others.TypeSort;
import ast.pass.others.TypeUplift;
import ast.pass.others.VarDeclToTop;
import ast.pass.others.VarDefSplitter;
import ast.pass.others.VarSort;
import ast.pass.reduction.BitLogicCategorizer;
import ast.pass.reduction.CompLinkReduction;
import ast.pass.reduction.CompositionReduction;
import ast.pass.reduction.ElementaryReduction;
import ast.pass.reduction.EnumLinkReduction;
import ast.pass.reduction.EnumReduction;
import ast.pass.reduction.FileReduction;
import ast.pass.reduction.ForReduction;
import ast.pass.reduction.NamespaceLinkReduction;
import ast.pass.reduction.OpenReplace;
import ast.pass.reduction.RangeReplacer;
import ast.pass.reduction.ReduceAliasType;
import ast.pass.reduction.ReduceMultiAssignment;
import ast.pass.reduction.ReduceRawComp;
import ast.pass.reduction.ReduceTuple;
import ast.pass.reduction.ReduceUnion;
import ast.pass.reduction.ReduceVarDefInit;
import ast.pass.reduction.SimplifyRef;
import ast.pass.reduction.StateLinkReduction;
import ast.pass.reduction.TupleAssignReduction;
import ast.pass.reduction.hfsm.HfsmReduction;
import ast.pass.specializer.StateVarInitExecutor;
import ast.pass.specializer.TypeEvalReplacerPass;
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

    // FuncHeaderReplacer.process(aclasses, kb); //TODO remove if not used

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

    passes.add(new InternsAdder());

    passes.add(new CheckNames());
    passes.add(new Linker());
    if (opt.getDocOutput()) {
      passes.add(new DocWriter());
    }
    passes.add(new FileReduction());

    passes.add(new TemplateArgEval());

    passes.add(new NamespaceLinkReduction());
    passes.add(new StateLinkReduction());
    passes.add(new EnumLinkReduction());
    passes.add(new CompLinkReduction());

    passes.add(new RootInstanceAdder());

    passes.add(new GlobalConstEval());
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
    RError.ass(item.pass.getPrecondition().isSatisfiedBy(ast), item.getName() + ": precondition does not hold");

    prefix = new Designator(prefix, item.getName());
    item.pass.process(ast, kb);
    dp.print(prefix.toString("."));

    RError.ass(item.pass.getPostcondition().isSatisfiedBy(ast), item.getName() + ": postcondition does not hold");
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
