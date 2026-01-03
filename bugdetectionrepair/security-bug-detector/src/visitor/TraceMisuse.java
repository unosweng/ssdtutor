package visitor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import controlflowgraph.backward.CFGNode;
import util.PrintHelper;
import util.UtilAST;

public class TraceMisuse extends FindMisuse {

	final String FOUND_VULNERABILITY = "Traced backward ARG and found";
	private int POSITION_TO_TRACK = 0;
	private int argPos = 0;
	private String ideMetInv;

	public TraceMisuse(DetectionASTVisitor detector) {
		this.detector = detector;
	}

	public void buildCallGraph(MethodDeclaration metDec) {
		nodeCount = 0;
		mapCFGNodes.clear();
		masterMethodSet.clear();

		IMethodBinding Mthbinding = metDec.resolveBinding();
		IJavaElement javaElem = Mthbinding.getJavaElement();
		if (javaElem instanceof IMethod) {
			IMethod vulneralbleCalleeMethod = (IMethod) javaElem;
			buildCallGraph(vulneralbleCalleeMethod, 0);
		}
	}

	public static String getQualifiedName(ClassInstanceCreation ciCre) {
		if (ciCre.resolveConstructorBinding() == null //
				|| ciCre.resolveConstructorBinding().getDeclaringClass() == null) {
			return null;
		}

		String quaName = ciCre.resolveConstructorBinding().getDeclaringClass().getQualifiedName();
		return quaName;
	}

	public void traceBackwardFromIndicator(MethodDeclaration metDec, MethodInvocation metInv, int posToTrack) throws Exception {
		nodeCount = 0;
		mapCFGNodes.clear();
		masterMethodSet.clear();

		this.POSITION_TO_TRACK = posToTrack;
		this.argPos = posToTrack;

		this.ideMetInv = metInv.getName().getIdentifier();
		detector.indicatorMethod = ideMetInv;
		detector.argumentPos = argPos;
		detector.indicatorPos = argPos;
		IMethodBinding Mthbinding = metDec.resolveBinding();
		IJavaElement javaElem = Mthbinding.getJavaElement();
		if (javaElem instanceof IMethod) {
			IMethod vulneralbleCalleeMethod = (IMethod) javaElem;
			buildCallGraph(vulneralbleCalleeMethod, 0);
			findCallSites();
			markCallSiteRootCallee(metInv);
			displayCFG();

			findSlices();
			findVulnerabilityInSlices();
		}
	}

	public void traceBackwardFromIndicator(MethodDeclaration metDec, ClassInstanceCreation ciCre, int posToTrack) throws Exception {
		nodeCount = 0;
		mapCFGNodes.clear();
		masterMethodSet.clear();

		this.POSITION_TO_TRACK = posToTrack;
		this.ideMetInv = ciCre.getType().toString();

		detector.indicatorMethod = ideMetInv;
		detector.argumentPos = argPos;
		detector.indicatorPos = argPos;

		IMethodBinding Mthbinding = metDec.resolveBinding();
		IJavaElement javaElem = Mthbinding.getJavaElement();
		if (javaElem instanceof IMethod) {
			IMethod vulneralbleCalleeMethod = (IMethod) javaElem;

			buildCallGraph(vulneralbleCalleeMethod, 0);
			findCallSites();
			markCallSiteRootCallee(ciCre);
			displayCFG();

			findSlices();
			findVulnerabilityInSlices();
		}

	}

	void findSlices() throws Exception {
		for (CFGNode iCaller : mapCFGNodes.values()) {
			if (iCaller.isLeaf()) { // the last caller of vulnerable callee
				TreeNode[] pathFromRootToLeaf = iCaller.getPath();
				for (TreeNode iNodeInPath : pathFromRootToLeaf) {
					defUseAnalysisForwardSlice((CFGNode) iNodeInPath);
					defUseAnalysisBackwardSlice((CFGNode) iNodeInPath);
				}
			}
		}
	}

	/*
	 * defUse Analysis Forward Slice
	 */
	private void defUseAnalysisForwardSlice(CFGNode n) {
		if (n.isRoot()) {
			n.findSlices(this.POSITION_TO_TRACK);
		} else {
			n.findSlices(-1);
		}
	}

	void defUseAnalysisBackwardSlice(CFGNode n) throws Exception {
		Object object = null;
		SimpleName varObj = null;
		if (n.isRoot() && -1 == n.getParmPositionToBackTrack()) {
			// Target exists in POSITION_TO_TRACK
			if (n.getRootCallSite() instanceof ClassInstanceCreation) {
				ClassInstanceCreation cicRootCallSite = (ClassInstanceCreation) n.getRootCallSite();
				object = cicRootCallSite.arguments().get(this.POSITION_TO_TRACK);
			} else if (n.getRootCallSite() instanceof MethodInvocation) {
				MethodInvocation miRootCallSite = (MethodInvocation) n.getRootCallSite();
				object = miRootCallSite.arguments().get(this.POSITION_TO_TRACK);
			} else {
				throw new Exception("Root Call Site - Unknown Instance");
			}

			// Find which parameter should be tracked backward.
			if (n.getMethodDec() != null && object != null) {
				if (object instanceof SimpleName) {
					varObj = (SimpleName) object;
				} else if (object instanceof SingleVariableDeclaration) {
					SingleVariableDeclaration svd = (SingleVariableDeclaration) object;
					varObj = svd.getName();
				} else if (object instanceof MethodInvocation) {
					// argument is a method invocation which must return a value
					MethodInvocation mi = (MethodInvocation) object;
					varObj = mi.getName();
					n.backTrackFromMethodInvocationArg(detector, mi);
				}
				List<?> parameters = n.getMethodDec().parameters();
				int matchedPos = findMatchedParameter(parameters, varObj);
				n.setParmPositionToBackTrack(matchedPos);
				n.setsNameToTrack(varObj);
			}

			// check if varObj matches a field declaration
			if (!n.isField() && n.getParmPositionToBackTrack() == -1) {
				SimpleName sFound = detector.checkVdfSetContains(varObj, true);
				if (sFound != null) {
					PrintHelper.printDebugMsg(n.getID() + " Found matching field declaration: " + sFound.getFullyQualifiedName());
					backTrackFieldDeclaration(n, sFound);
				}
			}
		}

		// # Trace backward from a call site with an affecting parameter.
		n.findBackwardSlices(detector, ideMetInv);
	}

	// trace field declaration
	private void backTrackFieldDeclaration(CFGNode n, SimpleName sName) {
		TypeDeclaration td = n.getTypeDec();
		MethodDeclaration[] methods = td.getMethods();
		for (MethodDeclaration md : methods) {
			md.accept(new ASTVisitor() {
				@Override
				public boolean visit(Assignment assignment) {
					Expression lhs = assignment.getLeftHandSide();
					if (lhs instanceof SimpleName) {
						SimpleName slhs = (SimpleName) lhs;
						if (slhs.resolveBinding().equals(sName.resolveBinding())) {
							// found a match
							Expression rhs = assignment.getRightHandSide();
							PrintHelper.printDebugMsg("LHS: " + sName.getFullyQualifiedName() + " in " + assignment.toString());
							// if RHS is a StringLiteral and matches searchString - we are done!
							if (rhs instanceof StringLiteral) {
								StringLiteral strLit = (StringLiteral) rhs;
								boolean isWeak = detector.checkWeakRules(detector.rules, ideMetInv, argPos, strLit.getLiteralValue());
								if (isWeak) {
									PrintHelper.printDebugMsg("RHS: SearchPattern found in " + assignment.toString());
									DetectionASTVisitor.getTree().buildCfgTree(detector, md, (MethodInvocation) n.getRootCallSite(), slhs);
									n.setField(true); // used to limit matching of field declaration
									return true;
								}
								// RHS is a SimpleName - backtrack parameter if necessary
							} else if (rhs instanceof SimpleName) {
								SimpleName srhs = (SimpleName) rhs;
								boolean ret = backTrackFromMethodDeclaration(md, srhs, n);
								if (ret) {
									n.setField(true);
									return true;
								}
							}

						}
					}
					return false;
				}
			});
		}
	}

	private boolean backTrackFromMethodDeclaration(MethodDeclaration md, SimpleName sName, CFGNode n) {

		// 1 - determine the position to backtrack
		int pos = findMatchedParameter(md.parameters(), sName);
		if (-1 == pos) {
			return false;
		}

		PrintHelper.printDebugMsg("Matched parameter at position: " + pos);

		// 2 - get the list of constructor invocations of this MD
		TypeDeclaration typeDec = n.getTypeDec();
		IMethodBinding mdBinding = md.resolveBinding();
		List<ConstructorInvocation> ciSet = new ArrayList<>();
		typeDec.accept(new ASTVisitor() {
			@Override
			public boolean visit(ConstructorInvocation ci) {
				IMethodBinding ciBinding = ci.resolveConstructorBinding();
				if (ciBinding != null && ciBinding.equals(mdBinding)) {
					ciSet.add(ci);
				}
				return true;
			}
		});

		// 3 - parse the list of CIs
		for (ConstructorInvocation ci : ciSet) {
			Object object = ci.arguments().get(pos);
			//         detector.indicatorPos = pos;
			if (object instanceof SimpleName) {
				SimpleName s = (SimpleName) object;
				// check to see if the setVarFieldUse contains this simpleName
				SimpleName sFound = detector.checkVdfSetContains(s, false);
				if (sFound != null) {
					PrintHelper.printDebugMsg(n.getID() + " Found matching field declaration: " + sFound.getFullyQualifiedName());
					DetectionASTVisitor.getTree().buildCfgTree(detector, md, (MethodInvocation) n.getRootCallSite(), sFound);
					n.setField(true);
					return true;
				} else {
					PrintHelper.printDebugMsg(s.getFullyQualifiedName() + " NOT found in VarDefUse set - back tracking needed");

					// back track further
					MethodDeclaration mdCI = UtilAST.findConstructorInvocationParent(ci);
					boolean ret = backTrackFromMethodDeclaration(mdCI, s, n);
					if (ret) {
						return true;
					}
				}
			} else if (object instanceof StringLiteral) {
				StringLiteral sl = (StringLiteral) object;
				boolean isWeak = detector.checkWeakRules(detector.rules, ideMetInv, argPos, sl.getLiteralValue());
				if (isWeak) {
					PrintHelper.printDebugMsg(n.getID() + " Found String Literal of Constructor Invocation: " + ci.resolveConstructorBinding().getName() + ":" + sl.getLiteralValue());
					DetectionASTVisitor.getTree().buildCfgTree(detector, md, (MethodInvocation) n.getRootCallSite(), ci);
					n.setField(true);
					return true;
				}
			}

		}

		return false;
	}

	@SuppressWarnings("unused")
	private boolean findMatchedName(CFGNode n, SimpleName sName) {
		for (ConstructorInvocation ci : n.getListConstructorInv()) {
			List<?> args = ci.arguments();
			if (args.size() == 0) {
				continue;
			}
			for (Object arg : args) {
				if (arg instanceof SimpleName) {
					SimpleName s = (SimpleName) arg;
					if (s.resolveBinding().equals(sName.resolveBinding())) {
						System.out.println("OK");
						return true;
					}
				}
			}
		}

		for (MethodInvocation mi : n.getListMethodInv()) {
			List<?> args = mi.arguments();
			if (args.size() == 0) {
				continue;
			}
			for (Object arg : args) {
				if (arg instanceof SimpleName) {
					SimpleName s = (SimpleName) arg;
					if (s.resolveBinding().equals(sName.resolveBinding())) {
						System.out.println("OK");
						return true;
					}
				}
			}
		}

		return false;
	}

	protected int findMatchedParameter(List<?> parameters, SimpleName spName) {
		int index = -1;

		if (spName == null || spName.resolveBinding() == null) {
			return index;
		}

		for (int i = 0; i < parameters.size(); i++) {
			Object iObj = parameters.get(i);
			if (iObj instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) iObj;
				SimpleName varName = svd.getName();

				if (varName == null || varName.resolveBinding() == null) {
					break;
				}

				if (spName.resolveBinding().equals(varName.resolveBinding())) {
					index = i;
					break;
				}
			}
		}
		return index;
	}

	void findVulnerabilityInSlices() {
		// System.out.println("[DBG]\t" + "nodeCount: " + nodeCount);
		for (CFGNode node : mapCFGNodes.values()) {
			if (node.getLevel() == 1) { // the last caller of vulnerable callee
				List<String> info2 = findVulnerabilityInSlice(node);
				for (String iMsg : info2) {
					if (iMsg.contains(FOUND_VULNERABILITY)) {
						detector.appendMsgToView(iMsg);
						PrintHelper.printDebugMsg(iMsg);

						// build the CFG tree
						DetectionASTVisitor.getTree().buildCfgTree(detector, node);
					}
				}
			}
		}
	}


	List<String> findVulnerabilityInSlice(DefaultMutableTreeNode leafNode) {

		int posToTrack = -1;

		List<String> listResult = new ArrayList<String>();
		TreeNode[] pathFromRootToLeaf = leafNode.getPath();

		for (TreeNode iNodeInPath : pathFromRootToLeaf) {
			listResult.add("\t" + iNodeInPath);
			CFGNode n = (CFGNode) iNodeInPath;

			// ignore nodes without a tracking position set
			if (-1 != n.getParmPositionToBackTrack()) {
				posToTrack = n.getParmPositionToBackTrack();
			}

			MethodInvocation callSiteMethInvc = n.getCallSiteMethodInvc();
			ConstructorInvocation callSiteConstrInvc = n.getCallSiteConstructorInvc();

			// back tracking - check call site invocations
			if (callSiteMethInvc != null) {
				String signature = UtilAST.getSignature(callSiteMethInvc);
				int offset = callSiteMethInvc.getStartPosition();
				checkSlices(listResult, n, signature, offset, posToTrack);
			} else if (callSiteConstrInvc != null) {
				String signature = UtilAST.getSignature(callSiteConstrInvc);
				int offset = callSiteConstrInvc.getStartPosition();
				checkSlices(listResult, n, signature, offset, posToTrack);
			}

			// forward tracking - check list of method invocations
			// int listSize = n.getListMethodInv().size();

		}
		return listResult;
	}

	private void checkSlices(List<String> listResult, CFGNode node, //
			String signature, int offset, int posToTrack) {

		if (null == node.getCallSiteMethodInvc() //
				|| null == node.getCallSiteMethodInvc().arguments()) {
			return;
		}

		int size = node.getCallSiteMethodInvc().arguments().size();
		if (-1 == posToTrack || posToTrack >= size) {
			return;
		}

		// if the callSiteMethodInv argument is not a SimpleName - just return
		Object obj = node.getCallSiteMethodInvc().arguments().get(posToTrack);
		if (false == obj instanceof SimpleName) {
			return;
		}

		SimpleName sName = (SimpleName) obj;
		int lineNumber = detector.getLineNumber(offset);
		listResult.add("\t\t" + signature + " at line: " + lineNumber);

		SimpleName foundSPName = null;
		LinkedHashSet<SimpleName> listSlices = node.getListSlices();
		for (SimpleName iSPName : listSlices) {

			// variable definition-use bind
			foundSPName = detector.checkVdfSetContains(iSPName, false);
			if (foundSPName != null && sName.getFullyQualifiedName().equals(foundSPName.getFullyQualifiedName())) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) foundSPName.getParent();
				if(detector.searchTypeRules.get(ideMetInv) != null) {
					if(detector.searchTypeRules.get(ideMetInv).get(argPos+1) != null) {
						detector.setSearchType(detector.searchTypeRules.get(ideMetInv).get(argPos+1).get("searchType"));
						boolean isWeak = detector.checkWeakRules(detector.rules, ideMetInv, argPos, vdf.getInitializer());
						if (isWeak) {
							foundSimpleName(listResult, node, foundSPName);
							break;
						}
					}
				}
			}

			// variable declaration fragment
			foundSPName = findVulnerabilityInSlice_VDF(iSPName);
			if (foundSPName != null && sName.getFullyQualifiedName().equals(foundSPName.getFullyQualifiedName())) {
				foundSimpleName(listResult, node, foundSPName);
				break;
			}

			// get bytes
			foundSPName = findVulnerabilityInSlice_GET_BYTES(iSPName);
			if (foundSPName != null && sName.getFullyQualifiedName().equals(foundSPName.getFullyQualifiedName())) {
				foundSimpleName(listResult, node, foundSPName);
				break;
			}
		}
	}

	private void foundSimpleName(List<String> listResult, CFGNode node, SimpleName foundSPName) {
		PrintHelper.printDebugMsg("OK - foundSPName is correct position of callSiteMethodInv");

		String fQualName = UtilAST.getSignature(foundSPName);
		int lineNum = detector.getLineNumber(foundSPName.getStartPosition());
		listResult.add(FOUND_VULNERABILITY + ": \"" + fQualName + "\" at line:" + lineNum);
		node.setRootCallSite(foundSPName);
	}

	public SimpleName findVulnerabilityInSlice_GET_BYTES(SimpleName spName) {
		MethodInvocation metInv = null;
		ASTNode astNode = UtilAST.findMethodInvocationParent(spName);
		if (astNode instanceof MethodInvocation) {
			metInv = (MethodInvocation) astNode;
		} else {
			return null;
		}

		String identifier = metInv.getName().getIdentifier();
		if (identifier.equals("getBytes")) {
			Expression expr = metInv.getExpression();

			if (expr instanceof SimpleName) {
				SimpleName spNameVar = (SimpleName) metInv.getExpression();
				String t = UtilAST.getType(spNameVar);
				final String CONST_TYPE_STRING = "String";

				if (t != null && t.equals(CONST_TYPE_STRING)) {
					return spNameVar;
				}
			}
		}
		return null;
	}

	SimpleName findVulnerabilityInSlice_VDF(SimpleName sn) {
		if (sn.getParent() instanceof VariableDeclarationFragment) {
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) sn.getParent();

			// first check to see if the Variable Declaration Fragment contains the search pattern
			Expression initializer = vdf.getInitializer();
			if (initializer instanceof StringLiteral) {
				StringLiteral strLiteral = (StringLiteral) initializer;
				boolean isWeak = detector.checkWeakRules(detector.rules, ideMetInv, POSITION_TO_TRACK, strLiteral.getLiteralValue());
				if (isWeak) {
					return vdf.getName();
				}
			} else if (initializer instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) initializer;
				traceForwardMethodInvocation(mi, sn);
			}
		}
		return null;
	}

	// does this method return a string that matches SearchPatterns
	public void traceForwardMethodInvocation(MethodInvocation mi, SimpleName s) {

		IMethodBinding iMethodBinding = mi.resolveMethodBinding().getMethodDeclaration();
		IJavaElement javaElement = iMethodBinding.getJavaElement();
		if (javaElement instanceof IMethod) {
			IMethod iMethod = (IMethod) javaElement;
			MethodDeclaration methodDec = UtilAST.findMethodDec(iMethod, detector.typeDecl);

			if (methodDec != null) {
				methodDec.accept(new ASTVisitor() {
					@Override
					public boolean visit(ReturnStatement rs) {
						if (detector.setReturnUse.contains(rs)) {
							PrintHelper.printDebugMsg("found root cause in Return Statement");
						}
						return true;
					}
				});
			}
		}
	}
}