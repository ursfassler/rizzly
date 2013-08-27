package evl.traverser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;

import util.ssa.BbEdge;
import util.ssa.DominanceFrontier;
import util.ssa.Dominator;
import util.ssa.PhiInserter;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.cfg.PhiStmt;
import evl.expression.reference.Reference;
import evl.function.FuncWithBody;
import evl.function.FunctionBase;
import evl.knowledge.KnowledgeBase;
import evl.statement.Assignment;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.statement.VarDefInitStmt;
import evl.statement.VarDefStmt;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
import evl.variable.Variable;

public class SsaMaker extends DefTraverser<Void, Void> {
  private KnowledgeBase kb;

  public SsaMaker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  static public void process(Evl evl, KnowledgeBase kb) {
    SsaMaker mbb = new SsaMaker(kb);
    mbb.traverse(evl, null);
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Void param) {
    Map<FuncVariable, SsaVariable> argmap = new HashMap<FuncVariable, SsaVariable>();
    ArrayList<Variable> arglist = new ArrayList<Variable>(obj.getParam().getList());
    obj.getParam().clear();
    for (Variable par : arglist) {
      assert (par instanceof FuncVariable);
      SsaVariable svar = new SsaVariable(par.getInfo(), par.getName(), par.getType());
      obj.getParam().add(svar);
      argmap.put((FuncVariable) par, svar);
    }

    if (obj instanceof FuncWithBody) {
      BasicBlockList body = (BasicBlockList) ((FuncWithBody) obj).getBody();

      DirectedGraph<BasicBlock, BbEdge> funcGraph = body.makeFuncGraph();
      Dominator<BasicBlock, BbEdge> dom = new Dominator<BasicBlock, BbEdge>(funcGraph);
      dom.calc();
      DominanceFrontier<BasicBlock, BbEdge> df = new DominanceFrontier<BasicBlock, BbEdge>(funcGraph, dom.getDom());
      df.calc();

      PhiInserter phi = new PhiInserter((FuncWithBody) obj, df);
      phi.doWork();

      Map<SsaVariable, FuncVariable> renamed = phi.getRenamed();

      SsaVarCreator ssa = new SsaVarCreator(renamed);
      ssa.traverse(body, null);

      addPhiArg(body, renamed);

      VariableLinker intra = new VariableLinker(kb, renamed);
      intra.traverse(body, null);

      InterBbVariableLinker.link(intra, dom.getDom(), body, argmap);

    }
    return null;
  }

  private void addPhiArg(BasicBlockList body, Map<SsaVariable, FuncVariable> renamed) {
    for (BasicBlock bb : body.getAllBbs()) {
      for (BasicBlock dst : bb.getEnd().getJumpDst()) {
        List<PhiStmt> phis = dst.getPhi();
        for (PhiStmt phistmt : phis) {
          FuncVariable var = renamed.get(phistmt.getVariable());
          assert (var != null);
          phistmt.addArg(bb, var); // just that a variable definition exists
        }
      }
    }
  }

}

class VariableLinker extends DefTraverser<Void, Map<FuncVariable, SsaVariable>> {
  private Map<BasicBlock, Map<FuncVariable, SsaVariable>> lastVarDef = new HashMap<BasicBlock, Map<FuncVariable, SsaVariable>>();
  private KnowledgeBase kb;
  private Map<SsaVariable, FuncVariable> renamed;

  public VariableLinker(KnowledgeBase kb, Map<SsaVariable, FuncVariable> renamed) {
    super();
    this.kb = kb;
    this.renamed = renamed;
  }

  public Map<BasicBlock, Map<FuncVariable, SsaVariable>> getLastVarDef() {
    return lastVarDef;
  }

  private static Variable replaceVar(Variable expr, Map<FuncVariable, SsaVariable> param) {
    if (param.containsKey(expr)) {
      return param.get(expr);
    } else {
      return expr;
    }
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Map<FuncVariable, SsaVariable> param) {
    visit(obj.getVariable(), param);
    return null;
  }

  private void handleVarWriter(SsaVariable newvar, Map<FuncVariable, SsaVariable> param) {
    FuncVariable oldvar = renamed.get(newvar);
    assert (oldvar != null);
    param.put(oldvar, newvar);
  }

  @Override
  protected Void visitPhiStmt(PhiStmt obj, Map<FuncVariable, SsaVariable> param) {
    visit(obj.getVariable(), param);
    handleVarWriter(obj.getVariable(), param);
    return null;
  }

  @Override
  protected Void visitVarDefInitStmt(VarDefInitStmt obj, Map<FuncVariable, SsaVariable> param) {
    visit(obj.getInit(), param);
    visit(obj.getVariable(), param);
    handleVarWriter(obj.getVariable(), param);
    return null;
  }

  @Override
  protected Void visitVariable(Variable obj, Map<FuncVariable, SsaVariable> param) {
    return null;
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, Map<FuncVariable, SsaVariable> param) {
    assert (!lastVarDef.containsKey(obj));

    assert (param == null);
    param = new HashMap<FuncVariable, SsaVariable>();
    super.visitBasicBlock(obj, param);
    lastVarDef.put(obj, new HashMap<FuncVariable, SsaVariable>(param));
    visitFollowingPhi(obj, param);
    return null;
  }

  /* handle phi functions as they belong to the previous basic blocks, what they actually do */
  private void visitFollowingPhi(BasicBlock bb, Map<FuncVariable, SsaVariable> param) {
    for (BasicBlock dst : bb.getEnd().getJumpDst()) {
      Collection<PhiStmt> phis = dst.getPhi();
      for (PhiStmt phi : phis) {
        Variable expr = phi.getArg(bb);
        if (expr != null) { // FIXME why?
          assert (expr != null);
          expr = replaceVar(expr, param);
          phi.addArg(bb, expr);
        } else {
          assert (false);
        }
      }
    }
  }

  @Override
  protected Void visitReference(Reference obj, Map<FuncVariable, SsaVariable> param) {
    if (obj.getLink() instanceof Variable) {
      obj.setLink(replaceVar((Variable) obj.getLink(), param));
    }
    return super.visitReference(obj, param);
  }

}

class SsaVarCreator extends NullTraverser<Void, List<Statement>> {
  private Map<SsaVariable, FuncVariable> renamed;
  private int nr = 0;

  public SsaVarCreator(Map<SsaVariable, FuncVariable> renamed) {
    this.renamed = renamed;
  }

  @Override
  protected Void visitDefault(Evl obj, List<Statement> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitBasicBlockList(BasicBlockList obj, List<Statement> param) {
    visitItr(obj.getAllBbs(), null);
    return null;
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, List<Statement> param) {
    assert (param == null);
    List<Statement> list = new ArrayList<Statement>(obj.getCode());
    obj.getCode().clear();
    for (Statement itr : list) {
      visit(itr, obj.getCode());
    }
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, List<Statement> param) {
    if (obj.getLeft().getOffset().isEmpty() && (obj.getLeft().getLink() instanceof FuncVariable)) {
      FuncVariable var = (FuncVariable) obj.getLeft().getLink();
      if (PhiInserter.isScalar(var.getType().getRef())) {
        nr++;
        SsaVariable sv = new SsaVariable(var, nr);
        VarDefInitStmt init = new VarDefInitStmt(obj.getInfo(), sv, obj.getRight());
        param.add(init);
        renamed.put(sv, var);
        return null;
      }
    }
    param.add(obj);
    return null;
  }
  
  @Override
  protected Void visitVarDef(VarDefStmt obj, List<Statement> param) {
    param.add(obj);
    return null;
  }

  @Override
  protected Void visitCallStmt(CallStmt obj, List<Statement> param) {
    param.add(obj);
    return null;
  }
}

class InterBbVariableLinker extends DefTraverser<Void, BasicBlock> {
  private VariableLinker link;
  private HashMap<BasicBlock, BasicBlock> idom;
  private Map<FuncVariable, SsaVariable> argmap;

  public static void link(VariableLinker link, HashMap<BasicBlock, BasicBlock> idom, BasicBlockList func, Map<FuncVariable, SsaVariable> argmap) {
    InterBbVariableLinker linker = new InterBbVariableLinker(link, idom, argmap);
    linker.visit(func, null);
  }

  public InterBbVariableLinker(VariableLinker link, HashMap<BasicBlock, BasicBlock> idom, Map<FuncVariable, SsaVariable> argmap) {
    super();
    this.link = link;
    this.idom = idom;
    this.argmap = argmap;
  }

  private SsaVariable getVariable(BasicBlock first, FuncVariable name, ElementInfo info) {
    for (BasicBlock dom = first; dom != null; dom = idom.get(dom)) {
      Map<FuncVariable, SsaVariable> lastDef = link.getLastVarDef().get(dom);
      if (lastDef.containsKey(name)) {
        return lastDef.get(name);
      }
    }

    if (argmap.containsKey(name)) { // it is a function argument
      return argmap.get(name);
    }

    RError.err(ErrorType.Error, info, "Variable definition not found: " + name);
    return null;
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, BasicBlock param) {
    assert (param == null);
    return super.visitBasicBlock(obj, obj);
  }

  @Override
  protected Void visitPhiStmt(PhiStmt obj, BasicBlock param) {
    for (BasicBlock in : obj.getInBB()) {
      Variable var = obj.getArg(in);
      if (var instanceof FuncVariable) {
        // the parameter <in> is correct since we execute the phi statement code in the previous basic block
        var = getVariable(in, (FuncVariable) var, obj.getInfo());
        obj.addArg(in, var);
      }
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, BasicBlock param) {
    if (obj.getOffset().isEmpty() && (obj.getLink() instanceof FuncVariable) && (PhiInserter.isScalar(((FuncVariable) obj.getLink()).getType().getRef()))) {
      BasicBlock dom = idom.get(param);
      if (dom != null) {
        // not for the first BB
        obj.setLink(getVariable(dom, (FuncVariable) obj.getLink(), obj.getInfo()));
      }
      return null;
    } else {
      return super.visitReference(obj, param);
    }
  }

}
