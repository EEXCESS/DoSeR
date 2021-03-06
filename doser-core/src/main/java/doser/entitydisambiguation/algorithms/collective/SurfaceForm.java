package doser.entitydisambiguation.algorithms.collective;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import doser.entitydisambiguation.backend.DisambiguationMainService;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;

public class SurfaceForm implements Comparable<SurfaceForm>, Cloneable {

	private int queryNr;
	private String surfaceForm;
	private String context;
	private List<String> candidates;
	private Integer ambiguity;
	private boolean isACandidate;
	private double difference;
	private Set<String> nouns;

	SurfaceForm(String surfaceForm, String context, List<String> candidates,
			int qryNr) {
		super();
		this.ambiguity = candidates.size();
		this.surfaceForm = surfaceForm;
		this.context = context;
		this.candidates = candidates;
		this.queryNr = qryNr;
		this.isACandidate = true;
		this.difference = 0;
//		this.nouns = extractNounsOfContext(context);
	}

	private Set<String> extractNounsOfContext(String context) {
		Parse topParses[] = ParserTool.parseLine(context,
				DisambiguationMainService.getInstance().getOpenNLP_parser(), 1);
		Set<String> nouns = new HashSet<String>();
		for (Parse p : topParses) {
			getNounPhrases(p, nouns);
		}
		return nouns;
	}

	private void getNounPhrases(Parse p, Set<String> set) {
		if (p.getType().equals("NN") || p.getType().equals("NNP")) {
			set.add(p.getCoveredText());
		}
		for (Parse child : p.getChildren()) {
			getNounPhrases(child, set);
		}
	}

	public void setCandidates(List<String> candidates) {
		this.candidates = candidates;
	}

	public List<String> getCandidates() {
		return candidates;
	}

	public void setACandidate(boolean can) {
		this.isACandidate = can;
	}

	public String getSurfaceForm() {
		return surfaceForm;
	}

	public boolean isACandidate() {
		return isACandidate;
	}

	public String getContext() {
		return context;
	}

	public int getQueryNr() {
		return queryNr;
	}

	public int getAmbiguity() {
		return this.ambiguity;
	}

	public void setDisambiguatedEntity(String url) {
		candidates.clear();
		candidates.add(url);
	}

	public void clearList() {
		candidates.clear();
	}

	public double getDifference() {
		return difference;
	}

	public void setDifference(double difference) {
		this.difference = difference;
	}

	@Override
	public int compareTo(SurfaceForm o) {
		if (this.difference < o.getDifference()) {
			return 1;
		} else if (this.difference > o.getDifference()) {
			return -1;
		} else {
			return 0;
		}
	}

	public Object clone() {
		ArrayList<String> newCandidates = new ArrayList<String>();
		for (String s : candidates) {
			newCandidates.add(s);
		}

		SurfaceForm n = new SurfaceForm(new String(this.surfaceForm),
				new String(this.context), newCandidates, this.queryNr);
		n.setACandidate(this.isACandidate);

		return n;
	}
}