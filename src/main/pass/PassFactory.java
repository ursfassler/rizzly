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

package main.pass;

import java.util.List;

import main.Configuration;
import ast.pass.check.model.Modelcheck;
import ast.pass.check.sanity.Sanitycheck;
import ast.pass.check.type.Typecheck;
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
import ast.pass.reduction.StateLinkReduction;
import ast.pass.reduction.TupleAssignReduction;
import ast.pass.reduction.hfsm.HfsmReduction;
import ast.pass.specializer.StateVarInitExecutor;
import ast.pass.specializer.TemplCallAdder;
import ast.pass.specializer.TypeCastAdder;
import ast.pass.specializer.TypeEvalPass;
import error.RError;
import error.RizzlyError;

//TODO combine CommandLineParser and PassFactory
public class PassFactory {
  public static PassGroup makePasses(Configuration configuration) {
    switch (configuration.passBuilding()) {
      case Automatic:
        return produceFullRizzlyPass(configuration);
      case Specified:
        return produce(configuration.passes());
    }

    return null;
  }

  private static PassGroup produce(List<String> passDescription) {
    PassGroup passes = new PassGroup("by argument");

    RizzlyError error = RError.instance();
    PassArgumentParser argumentParser = new PassArgumentParser(error);
    ExplicitPassesFactory factory = new ExplicitPassesFactory(argumentParser, error);

    for (String pass : passDescription) {
      passes.add(factory.produce(pass));
    }

    return passes;
  }

  private static PassGroup produceFullRizzlyPass(Configuration configuration) {
    // TODO split up

    PassGroup passes = new PassGroup("ast");
    passes.checks.add(new Sanitycheck());

    passes.passes.add(funPasses(configuration));

    passes.add(new ConstTyper());

    if (!configuration.doLazyModelCheck()) {
      // TODO check that model check does not change AST
      passes.add(new Modelcheck());
    }

    passes.passes.add(reduce(passes, configuration));

    passes.add(new InitVarTyper());
    passes.add(new BitLogicCategorizer());
    passes.add(new Typecheck());
    passes.add(new SystemIfaceAdder());

    passes.add(new RetStructIntroducer());
    passes.add(new TupleAssignReduction());
    // passes.add(new ExprCutter());

    if (configuration.doDebugEvent()) {
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

    passes.passes.add(optimize(passes, configuration));

    passes.passes.add(prepareForC(passes, configuration));

    passes.add(new CWriter());
    return passes;
  }

  private static PassGroup funPasses(Configuration configuration) {
    PassGroup passes = new PassGroup("fun");

    passes.add(FileLoader.create(configuration));

    passes.add(new InternsAdder());

    passes.add(new CheckNames());
    passes.add(new Linker());
    if (configuration.doDocOutput()) {
      passes.add(new DocWriter());
    }
    passes.add(new FileReduction());

    passes.add(new NamespaceLinkReduction());
    passes.add(new StateLinkReduction());
    passes.add(new EnumLinkReduction());
    passes.add(new CompLinkReduction());

    passes.add(new RootInstanceAdder());

    // passes.add(new GlobalConstEval());
    passes.add(new TemplCallAdder());
    passes.add(new TypeCastAdder());
    passes.add(new TypeEvalPass());

    passes.add(new VarDefSplitter());
    passes.add(new UnusedRemover());
    passes.add(new StateVarInitExecutor());
    passes.add(new UnusedRemover());

    // what was in FunToEvl
    passes.add(new ReduceMultiAssignment());
    passes.add(new ReduceRawComp());
    // passes.add(new SimplifyRef());
    // passes.add(new CheckSimpleTypeRef());
    passes.add(new ReduceVarDefInit());

    return passes;
  }

  private static PassGroup prepareForC(PassGroup passes, Configuration configuration) {
    PassGroup cprep = new PassGroup("cout");
    cprep.checks.addAll(passes.checks);

    cprep.add(new VarDeclToTop());
    cprep.add(new TypeMerge());
    cprep.add(new CRenamer());
    cprep.add(new TypeSort());
    cprep.add(new VarSort());
    return cprep;
  }

  private static PassGroup optimize(PassGroup passes, Configuration configuration) {
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
  private static PassGroup reduce(PassGroup passes, Configuration configuration) {
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

}
