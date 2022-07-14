package org.yeastrc.limelight.xml.comet_mzid.objects;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public class CometResults {

	private Map<MzidReportedPeptide, Collection<CometPSM>> peptidePSMMap;

	private Map<String, String> proteinsIdSequenceMap;
	private Map<String, MzidProtein> proteinsSequenceProteinMap;

	private Map<String, BigDecimal> staticMods;
	private String version;
	private String searchDatabase;

	public String getSearchDatabase() {
		return searchDatabase;
	}

	public void setSearchDatabase(String searchDatabase) {
		this.searchDatabase = searchDatabase;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<MzidReportedPeptide, Collection<CometPSM>> getPeptidePSMMap() {
		return peptidePSMMap;
	}

	public void setPeptidePSMMap(Map<MzidReportedPeptide, Collection<CometPSM>> peptidePSMMap) {
		this.peptidePSMMap = peptidePSMMap;
	}

	public Map<String, String> getProteinsIdSequenceMap() {
		return proteinsIdSequenceMap;
	}

	public void setProteinsIdSequenceMap(Map<String, String> proteinsIdSequenceMap) {
		this.proteinsIdSequenceMap = proteinsIdSequenceMap;
	}

	public Map<String, MzidProtein> getProteinsSequenceProteinMap() {
		return proteinsSequenceProteinMap;
	}

	public void setProteinsSequenceProteinMap(Map<String, MzidProtein> proteinsSequenceProteinMap) {
		this.proteinsSequenceProteinMap = proteinsSequenceProteinMap;
	}

	public Map<String, BigDecimal> getStaticMods() {
		return staticMods;
	}

	public void setStaticMods(Map<String, BigDecimal> staticMods) {
		this.staticMods = staticMods;
	}
}
