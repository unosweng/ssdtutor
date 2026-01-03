/*
 * @(#) Test01.java
 *
 * Copyright 2013 The Software Evolution and Analysis Laboratory Lab 
 * Electrical and Computer Engineering, The University of Texas at Austin
 * ACES 5.118, C5000, 201 E 24th Street, Austin, TX 78712-0240
 */
package changedistiller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.NodePair;

/**
 * @author Myoungkyu Song
 * @date Oct 22, 2013
 * @since J2SE-1.5 (Java SE 7 [1.7.0_40])
 */
public class UTChangeDistiller extends UTJavaDistiller {
	private List<SourceCodeChange>				mChanges;
	private List<NodePair>							mMatches;
	private List<SourceCodeChange>				mInsertList			= new ArrayList<SourceCodeChange>();
	private List<SourceCodeChange>				mDeleteList			= new ArrayList<SourceCodeChange>();
	private List<SourceCodeChange>				mUpdateList			= new ArrayList<SourceCodeChange>();
	private List<SourceCodeChange>				mMoveList			= new ArrayList<SourceCodeChange>();

	private HashMap<SourceCodeChange, Node>	mDeletePair			= new HashMap<SourceCodeChange, Node>();
	private HashMap<SourceCodeChange, Node>	mInsertPair			= new HashMap<SourceCodeChange, Node>();
	private List<Node>								mDeleteNodeList	= new ArrayList<Node>();
	private List<Node>								mInsertNodeList	= new ArrayList<Node>();

	/**
	 * Compute the difference of two nodes.
	 */
	public List<SourceCodeChange> diff(Node rootOld, Node rootNew) {
		StructureEntityVersion structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, "Method", 0);
		Distiller distiller = getDistiller(structureEntity);
		distiller.extractClassifiedSourceCodeChanges(rootOld, rootNew);
		mChanges = structureEntity.getSourceCodeChanges();
		return structureEntity.getSourceCodeChanges();
	}

	/**
	 * Compute the difference of two methods.
	 */
	public List<SourceCodeChange> diffMethod(Node aRootOldRev, Node aRootNewRev) {
		StructureEntityVersion structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, "Method", 0);
		Distiller distiller = getDistiller(structureEntity);
		distiller.extractClassifiedSourceCodeChanges(aRootOldRev, aRootNewRev);
		mChanges = structureEntity.getSourceCodeChanges();
		mMatches = new ArrayList<NodePair>();
		mMatches.addAll(distiller.getMatch());
		Collections.sort(mMatches, new Comparator<NodePair>() {
			@Override
			public int compare(NodePair o1, NodePair o2) {
				return o1.getLeft().getEntity().getStartPosition() - o2.getLeft().getEntity().getStartPosition();
			}
		});
		inspectChanges(aRootOldRev, aRootNewRev);
		return mChanges;
	}

	/**
	 * Compute the difference of two blocks.
	 */
	public List<SourceCodeChange> diffBlock(Node aRootOld, Node aRootNew) {
		Node ndRootOldRev = null, ndRootNewRev = null;

		if (aRootOld.getEntity().getLabel().equals("METHOD")) {
			ndRootOldRev = aRootOld;
		} else {
			ndRootOldRev = new Node(JavaEntityType.METHOD, "MethodWrapper");
			ndRootOldRev.setEntity(new SourceCodeEntity("MethodWrapper", JavaEntityType.METHOD, new SourceRange()));
			ndRootOldRev.add(aRootOld);
		}
		if (aRootNew.getEntity().getLabel().equals("METHOD")) {
			ndRootNewRev = aRootNew;
		} else {
			ndRootNewRev = new Node(JavaEntityType.METHOD, "MethodWrapper");
			ndRootNewRev.setEntity(new SourceCodeEntity("MethodWrapper", JavaEntityType.METHOD, new SourceRange()));
			ndRootNewRev.add(aRootNew);
		}
		StructureEntityVersion structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, "Method", 0);
		Distiller distiller = getDistiller(structureEntity);
		distiller.extractClassifiedSourceCodeChanges(ndRootOldRev, ndRootNewRev);
		mChanges = structureEntity.getSourceCodeChanges();
		mMatches = new ArrayList<NodePair>();
		mMatches.addAll(distiller.getMatch());
		Collections.sort(mMatches, new Comparator<NodePair>() {
			@Override
			public int compare(NodePair o1, NodePair o2) {
				return o1.getRight().getEntity().getStartPosition() - o2.getRight().getEntity().getStartPosition();
			}
		});
		inspectChanges(aRootOld, aRootNew);
		return mChanges;
	}

	/**
	 * Inspect changes
	 */
	private void inspectChanges(Node aRootOld, Node aRootNew) {
		for (int i = 0; i < mChanges.size(); i++) {
			SourceCodeChange change = mChanges.get(i);
			if (change instanceof Update) {
				// Update chUpdate = (Update) change;
				// System.out.println("[DBG0] " + chUpdate);
				mUpdateList.add(change);
			} else if (change instanceof Move) {
				// Move chMove = (Move) change;
				// System.out.println("[DBG1] " + chMove);
				mMoveList.add(change);
			} else if (change instanceof Insert) {
				// Insert chInsert = (Insert) change;
				// System.out.println("[DBG2] " + chInsert.getChangedEntity().getLabel() + " " + //
				// chInsert.getChangedEntity().getUniqueName());
				Node iNode = null;
				Enumeration<?> e = aRootNew.preorderEnumeration();
				while (e.hasMoreElements()) {
					iNode = (Node) e.nextElement();
					if (iNode.getEntity().equals(change.getChangedEntity())) {
						// iNode.setInsert(true);
						mInsertPair.put(change, iNode);
						mInsertNodeList.add(iNode);
						break;
					}
				}
				mInsertList.add(change);
			} else if (change instanceof Delete) {
				// Delete chDelete = (Delete) change;
				// System.out.println("[DBG3] " + chDelete.getChangedEntity().getLabel() + " " + //
				// chDelete.getChangedEntity().getUniqueName());
				Node iNode = null;
				Enumeration<?> e = aRootOld.preorderEnumeration();
				while (e.hasMoreElements()) {
					iNode = (Node) e.nextElement();
					if (iNode.getEntity().equals(change.getChangedEntity())) {
						// iNode.setDelete(true);
						mDeletePair.put(change, iNode);
						mDeleteNodeList.add(iNode);
						break;
					}
				}
				mDeleteList.add(change);
			} else {
				throw new RuntimeException("LOOK AT HERE, PLEASE!!");
			}
		}
		sortNode(mInsertNodeList);
		sortNode(mDeleteNodeList);
		sortChanges();
	}

	/**
	 * Sort changes
	 */
	void sortChanges() {
		sortChange(mUpdateList);
		sortChange(mMoveList);
		sortChange(mInsertList);
		sortChange(mDeleteList);
	}

	/**
	 * Sort change
	 */
	void sortChange(List<SourceCodeChange> aList) {
		Collections.sort(aList, new Comparator<SourceCodeChange>() {
			@Override
			public int compare(SourceCodeChange o1, SourceCodeChange o2) {
				return o1.getChangedEntity().getStartPosition() - o2.getChangedEntity().getStartPosition();
			}
		});
	}

	void sortNode(List<Node> aList) {
		Collections.sort(aList, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o1.getEntity().getStartPosition() - o2.getEntity().getStartPosition();
			}
		});
	}

	/**
	 * Print changes
	 */
	public void printChanges() {
		if (mChanges == null) {
			System.out.println("[INFO] THERE IS NO CHANGE.");
		} else {
			for (int i = 0; i < mChanges.size(); i++) {
				SourceCodeChange change = mChanges.get(i);
				SourceCodeEntity newEntity = null, changedEntity = null;

				if (change instanceof Update) {
					Update update = (Update) change;
					newEntity = update.getNewEntity();
				} else if (change instanceof Move) {
					Move move = (Move) change;
					newEntity = move.getNewEntity();
				}

				System.out.println("   * " + change.getChangeType());
				changedEntity = change.getChangedEntity();
				getChangedInfo(changedEntity, change);
				if (newEntity != null) {
					System.out.println("        :     ");
					System.out.println("        V     ");
				} else
					System.out.println();
				getChangedInfo(newEntity, change);
				System.out.println("DBG__________________________________________");
			}
		}
		System.out.println("==========================================");
	}

	/**
	 * Gets the changed info.
	 */
	public void getChangedInfo(SourceCodeEntity changedEntity, SourceCodeChange change) {
		if (changedEntity == null) {
			return;
		}
		SourceRange changedRange = changedEntity.getSourceRange();
		String changeName = changedEntity.getUniqueName();
		String changeType = changedEntity.getType().toString();
		// SourceCodeEntity parent = change.getParentEntity();
		// String parentName = parent.getUniqueName();
		// SourceRange parentChangedRange = parent.getSourceRange();
		System.out.println("[DBG] " + changedRange + ", (" + changeType + ")" + "(" + changeName + ")"//
		/*+ ", PARENT: " + parent.getType() + parent.getSourceRange()*/);
	}

	/**
	 * Prints the changed info.
	 */
	public void printChangedInfo(SourceCodeEntity changedEntity, SourceCodeChange change) {
		if (changedEntity == null) {
			return;
		}
		System.out.println("   * " + change.getChangeType());
		SourceRange changedRange = changedEntity.getSourceRange();
		String changeName = changedEntity.getUniqueName();
		// SourceCodeEntity parent = change.getParentEntity();
		// String parentName = parent.getUniqueName();
		// SourceRange parentChangedRange = parent.getSourceRange();
		System.out.println("[DBG] OFFSET: " + changedRange + ", CHANGE: [" + changeName + "]"//
		/*+ ", PARENT: " + parent.getType() + parent.getSourceRange()*/);
	}

	/**
	 * Set changes
	 */
	public void setChanges(List<SourceCodeChange> aChanges) {
		this.mChanges = aChanges;
	}

	/**
	 * Get changes
	 */
	public List<SourceCodeChange> getChanges() {
		return mChanges;
	}

	/**
	 * Get matches
	 */
	public List<NodePair> getMatches() {
		return mMatches;
	}

	/**
	 * Get an insert List
	 */
	public List<SourceCodeChange> getInsertList() {
		return mInsertList;
	}

	/**
	 * Get a delete list
	 */
	public List<SourceCodeChange> getDeleteList() {
		return mDeleteList;
	}

	/**
	 * Set an update list
	 */
	public List<SourceCodeChange> getUpdateList() {
		return mUpdateList;
	}

	/**
	 * Get a move list
	 */
	public List<SourceCodeChange> getMoveList() {
		return mMoveList;
	}

	/**
	 * Get a delete pair
	 */
	public HashMap<SourceCodeChange, Node> getDeletePair() {
		return mDeletePair;
	}

	/**
	 * Get an insert pair
	 */
	public HashMap<SourceCodeChange, Node> getInsertPair() {
		return mInsertPair;
	}

	/**
	 * Get delete node list
	 */
	public List<Node> getDeleteNodeList() {
		return mDeleteNodeList;
	}

	/**
	 * Get insert node list
	 */
	public List<Node> getInsertNodeList() {
		return mInsertNodeList;
	}
}
