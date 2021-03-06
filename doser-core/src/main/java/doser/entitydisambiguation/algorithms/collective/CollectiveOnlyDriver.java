package doser.entitydisambiguation.algorithms.collective;

import java.util.LinkedList;
import java.util.List;

import doser.entitydisambiguation.dpo.Response;
import doser.entitydisambiguation.knowledgebases.EntityCentricKnowledgeBaseDefault;

public class CollectiveOnlyDriver extends AlgorithmDriver {

	public static final int MAXSURFACEFORMSPERQUERY = 50;
	public static final int MULTIPLIER = 4;
	public static final int CLUSTERSIZE = 5;

	public CollectiveOnlyDriver(Response[] res,
			List<SurfaceForm> rep,
			EntityCentricKnowledgeBaseDefault eckb) {
		super(res, rep, eckb);
	}

	@Override
	public void solve() {
		List<SurfaceForm> finalList = new LinkedList<SurfaceForm>();

		if (this.rep.size() > MAXSURFACEFORMSPERQUERY) {
			List<SurfaceForm> disambiguatedSFs = new LinkedList<SurfaceForm>();
			for (SurfaceForm c : rep) {
				if (c.getCandidates().size() == 1) {
					disambiguatedSFs.add(c);
				}
			}
			int counter = 0;
			while (true) {
				if ((counter + MAXSURFACEFORMSPERQUERY) < this.rep.size()) {
					List<SurfaceForm> subList = this.rep
							.subList(counter,
									(counter + MAXSURFACEFORMSPERQUERY));
					finalList.addAll(miniSolve(subList));
					counter += MAXSURFACEFORMSPERQUERY;
				} else {
					List<SurfaceForm> subList = this.rep
							.subList(counter, this.rep.size());
					finalList.addAll(miniSolve(subList));
					break;
				}
			}

		} else {
			finalList.addAll(miniSolve(this.rep));
		}
		this.rep = finalList;
	}

	private List<SurfaceForm> miniSolve(
			List<SurfaceForm> rep) {
		List<SurfaceForm> sol = new LinkedList<SurfaceForm>();
		List<LinkedList<SurfaceForm>> clusters = createDivideAndConquerClusters(rep);
		boolean reduced = false;
		while (clusters.size() > 1) {
			for (LinkedList<SurfaceForm> cluster : clusters) {
				CandidateElimination elimination = new CandidateElimination(
						cluster, eckb, clusters.size() * MULTIPLIER, this.rep);
				elimination.solve();
			}
			clusters = merge(clusters);
			reduced = true;
		}

		LinkedList<SurfaceForm> cluster = clusters.get(0);

		// If no CandidateElimination was performed due to only cluster is
		// available, we have to perform a CandidateElimination if more than one
		// surface form are available.
		// if (!reduced && cluster.size() > 1) {
		// int max = 0;
		// while ((max = computeMaxCandidates(cluster)) > 10) {
		// int nrCandidates = (int) Math.floor(((double) max) * 0.66);
		// CandidateElimination elimination = new CandidateElimination(
		// cluster, eckb, nrCandidates, this.rep);
		// elimination.solve();
		// }
		// }

		PageRankDisambiguator disambiguator = new PageRankDisambiguator(
				cluster, eckb.getFeatureDefinition());
		disambiguator.solve();
		sol.addAll(disambiguator.getRepresentation());
		return sol;
	}

	private int computeMaxCandidates(List<SurfaceForm> reps) {
		int max = 0;
		for (SurfaceForm sf : reps) {
			if (sf.getCandidates().size() > max) {
				max = sf.getCandidates().size();
			}
		}
		return max;
	}

	private List<LinkedList<SurfaceForm>> merge(
			List<LinkedList<SurfaceForm>> oldReps) {
		List<LinkedList<SurfaceForm>> newClusters = new LinkedList<LinkedList<SurfaceForm>>();
		for (int i = 0; i < oldReps.size(); i = i + 2) {
			LinkedList<SurfaceForm> l = new LinkedList<SurfaceForm>();
			if (i < oldReps.size()) {
				addAllWithoutNoCandidates(l, oldReps.get(i));
			}
			if ((i + 1) < oldReps.size()) {
				addAllWithoutNoCandidates(l, oldReps.get(i + 1));
			}
			newClusters.add(l);
		}
		return newClusters;
	}

	private void addAllWithoutNoCandidates(
			LinkedList<SurfaceForm> l,
			LinkedList<SurfaceForm> old) {
		for (SurfaceForm col : old) {
			if (col.isACandidate()) {
				l.add(col);
			}
		}
	}

	private List<LinkedList<SurfaceForm>> createDivideAndConquerClusters(
			List<SurfaceForm> rep) {
		int nrclusters = (int) Math.ceil((double) rep.size()
				/ (double) CLUSTERSIZE);
		System.out.println("Detected Clustersize: " + nrclusters);
		List<LinkedList<SurfaceForm>> clusters = new LinkedList<LinkedList<SurfaceForm>>();
		List<SurfaceForm> unambiguous = detectDisambiguatedSufaceForms(rep);
		for (int i = 0; i < nrclusters; i++) {
			LinkedList<SurfaceForm> list = new LinkedList<SurfaceForm>();
			int counter = 0;
			while (true) {
				if (counter > 0
						&& (counter % CLUSTERSIZE == 0 || (i * CLUSTERSIZE + counter) == rep
								.size())) {
					break;
				}
				list.add(rep.get(i * CLUSTERSIZE + counter));
				counter++;
			}
			list.addAll(unambiguous);
			clusters.add(list);
		}

		// Fill Last Cluster with an appropriate amount of Surface Forms
		LinkedList<SurfaceForm> lastCluster = clusters
				.get(clusters.size() - 1);
		if (clusters.size() > 1 && lastCluster.size() < CLUSTERSIZE) {
			LinkedList<SurfaceForm> forelast = clusters
					.get(clusters.size() - 2);
			int counter = forelast.size() - 1;
			while (lastCluster.size() < CLUSTERSIZE) {
				SurfaceForm col = forelast.get(counter);
				SurfaceForm newCol = (SurfaceForm) col
						.clone();
				newCol.setACandidate(false);
				lastCluster.add(newCol);
				counter--;
			}
		}
		return clusters;
	}

	private List<SurfaceForm> detectDisambiguatedSufaceForms(
			List<SurfaceForm> reps) {
		List<SurfaceForm> unambiguous = new LinkedList<SurfaceForm>();
		for (SurfaceForm rep : reps) {
			if (rep.getCandidates().size() == 1) {
				SurfaceForm clone = (SurfaceForm) rep
						.clone();
				clone.setACandidate(false);
				unambiguous.add(clone);
			}
		}
		return unambiguous;
	}

	class DivideAndConquerCluster {

		private List<SurfaceForm> rep;

		DivideAndConquerCluster(List<SurfaceForm> reps) {
			super();
			this.rep = reps;
		}

		List<SurfaceForm> getCollectiveSFRepresentations() {
			return this.rep;
		}
	}
}