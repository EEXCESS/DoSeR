package doser.webclassify.dpo;

import java.util.List;

import doser.language.Languages;

public class Document {

	/**
	 * Maybe some more metadata
	 */
	private List<Paragraph> paragraphs;
	
	private String language;

	public List<Paragraph> getParagraphs() {
		return paragraphs;
	}

	public void setParagraphs(List<Paragraph> paragraphs) {
		this.paragraphs = paragraphs;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}	
	
	public Languages getInternLanguage() {
		if(this.language != null) {
			if(this.language.equalsIgnoreCase("de")) {
				return Languages.german;
			} else {
				return Languages.english;
			}
		} else {
			return Languages.english;
		}
	}
}
