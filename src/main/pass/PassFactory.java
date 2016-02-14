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

import main.Configuration;
import ast.pass.check.model.Modelcheck;
import ast.pass.check.sanity.Sanitycheck;
import ast.pass.check.type.Typecheck;
import ast.pass.input.xml.XmlParserPass;
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
import ast.pass.others.DefaultVisitorPass;
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
import ast.pass.output.xml.XmlWriterPass;
import ast.pass.reduction.BitLogicCategorizer;
import ast.pass.reduction.CompLinkReduction;
import ast.pass.reduction.CompositionReduction;
import ast.pass.reduction.ElementaryReduction;
import ast.pass.reduction.EnumLinkReduction;
import ast.pass.reduction.EnumReduction;
import ast.pass.reduction.FileReduction;
import ast.pass.reduction.ForReduction;
import ast.pass.reduction.MetadataRemover;
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

public class PassFactory {
  public static PassGroup makePasses(Configuration configuration) {
    switch (configuration.parseAs()) {
      case Rizzly:
        if (configuration.doXml()) {
          return produceXmlPass(configuration);
        } else {
          return produceFullRizzlyPass(configuration);
        }
      case Xml:
        return produceXml2xml(configuration);
    }
    return null;
  }

  private static PassGroup produceXml2xml(Configuration configuration) {
    PassGroup passes = new PassGroup("xml2xml");
    passes.add(new XmlParserPass(configuration));
    passes.add(new XmlWriterPass(configuration));
    return passes;
  }

  private static PassGroup produceFullRizzlyPass(Configuration configuration) {
    // TODO split up

    PassGroup passes = new PassGroup("ast");
    passes.checks.add(new Sanitycheck(configuration));

    passes.passes.add(funPasses(configuration));

    passes.add(new ConstTyper(configuration));

    if (!configuration.doLazyModelCheck()) {
      // TODO check that model check does not change AST
      passes.add(new Modelcheck(configuration));
    }

    passes.passes.add(reduce(passes, configuration));

    passes.add(new InitVarTyper(configuration));
    passes.add(new BitLogicCategorizer(configuration));
    passes.add(new Typecheck(configuration));
    passes.add(new SystemIfaceAdder(configuration));

    passes.add(new RetStructIntroducer(configuration));
    passes.add(new TupleAssignReduction(configuration));
    // passes.add(new ExprCutter());

    if (configuration.doDebugEvent()) {
      passes.add(new DebugIface(configuration));
      // only for debugging
      // passes.add(TypeChecker.class);
    }

    passes.add(new AlwaysGreater(configuration));

    passes.add(new ForReduction(configuration));

    passes.add(new TypeUplift(configuration));
    passes.add(new CompareReplacer(configuration));
    passes.add(new RangeConverter(configuration));

    passes.add(new BitnotFixer(configuration));

    passes.add(new Instantiation(configuration));

    passes.add(new HeaderWriter(configuration));

    passes.add(new ConstantPropagation(configuration));

    // Have to do it here since queue reduction creates enums
    passes.add(new EnumReduction(configuration));
    passes.add(new ConstantPropagation(configuration));

    passes.add(new RemoveUnused(configuration));

    passes.add(new IfCutter(configuration));

    passes.add(new RangeReplacer(configuration));
    passes.add(new RemoveUnused(configuration));

    passes.passes.add(optimize(passes, configuration));

    passes.passes.add(prepareForC(passes, configuration));

    passes.add(new CWriter(configuration));
    return passes;
  }

  private static PassGroup produceXmlPass(Configuration configuration) {
    PassGroup passes = new PassGroup("ast");
    passes.add(new FileLoader(configuration));
    passes.add(new DefaultVisitorPass(new MetadataRemover(), configuration));
    passes.add(new XmlWriterPass(configuration));
    return passes;
  }

  private static PassGroup funPasses(Configuration configuration) {
    PassGroup passes = new PassGroup("fun");

    passes.add(new FileLoader(configuration));

    passes.add(new InternsAdder(configuration));

    passes.add(new CheckNames(configuration));
    passes.add(new Linker(configuration));
    if (configuration.doDocOutput()) {
      passes.add(new DocWriter(configuration));
    }
    passes.add(new FileReduction(configuration));

    passes.add(new NamespaceLinkReduction(configuration));
    passes.add(new StateLinkReduction(configuration));
    passes.add(new EnumLinkReduction(configuration));
    passes.add(new CompLinkReduction(configuration));

    passes.add(new RootInstanceAdder(configuration));

    // passes.add(new GlobalConstEval());
    passes.add(new TemplCallAdder(configuration));
    passes.add(new TypeCastAdder(configuration));
    passes.add(new TypeEvalPass(configuration));

    passes.add(new VarDefSplitter(configuration));
    passes.add(new UnusedRemover(configuration));
    passes.add(new StateVarInitExecutor(configuration));
    passes.add(new UnusedRemover(configuration));

    // what was in FunToEvl
    passes.add(new ReduceMultiAssignment(configuration));
    passes.add(new ReduceRawComp(configuration));
    // passes.add(new SimplifyRef());
    // passes.add(new CheckSimpleTypeRef());
    passes.add(new ReduceVarDefInit(configuration));

    return passes;
  }

  private static PassGroup prepareForC(PassGroup passes, Configuration configuration) {
    PassGroup cprep = new PassGroup("cout");
    cprep.checks.addAll(passes.checks);

    cprep.add(new VarDeclToTop(configuration));
    cprep.add(new TypeMerge(configuration));
    cprep.add(new CRenamer(configuration));
    cprep.add(new TypeSort(configuration));
    cprep.add(new VarSort(configuration));
    return cprep;
  }

  private static PassGroup optimize(PassGroup passes, Configuration configuration) {
    PassGroup optimize = new PassGroup("optimize");
    optimize.checks.addAll(passes.checks);

    optimize.add(new BlockReduction(configuration));
    optimize.add(new NoCallEmptyFunc(configuration));
    optimize.add(new RemoveUnused(configuration));
    optimize.add(new FuncInliner(configuration));
    optimize.add(new TautoExprDel(configuration));
    optimize.add(new TautoStmtDel(configuration));
    optimize.add(new RemoveUnused(configuration));
    optimize.add(new BlockReduction(configuration));
    return optimize;
  }

  // TODO separate things / better naming
  private static PassGroup reduce(PassGroup passes, Configuration configuration) {
    PassGroup reduction = new PassGroup("reduction");
    reduction.checks.addAll(passes.checks);
    reduction.add(new IntroduceConvert(configuration));
    reduction.add(new OpenReplace(configuration));
    reduction.add(new ElementaryReduction(configuration));
    reduction.add(new CompositionReduction(configuration));
    reduction.add(new HfsmReduction(configuration));
    reduction.add(new ReduceAliasType(configuration));
    reduction.add(new ReduceUnion(configuration));
    reduction.add(new ReduceTuple(configuration));
    return reduction;
  }

}
