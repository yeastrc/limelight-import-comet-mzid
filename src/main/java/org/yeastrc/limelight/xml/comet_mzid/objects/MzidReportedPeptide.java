package org.yeastrc.limelight.xml.comet_mzid.objects;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class MzidReportedPeptide {

	@Override
	public String toString() {
		return "MzidReportedPeptide{" +
				"reportedPeptideString='" + reportedPeptideString + '\'' +
				", nakedPeptide='" + nakedPeptide + '\'' +
				", mods=" + mods +
				", proteinMatches=" + proteinMatches +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MzidReportedPeptide that = (MzidReportedPeptide) o;
		return reportedPeptideString.equals(that.reportedPeptideString);
	}

	@Override
	public int hashCode() {
		return Objects.hash(reportedPeptideString);
	}

	private String reportedPeptideString;
	private String nakedPeptide;
	private Map<Integer, BigDecimal> mods;
	private Collection<String> proteinMatches;

	public Collection<String> getProteinMatches() {
		return proteinMatches;
	}

	public void setProteinMatches(Collection<String> proteinMatches) {
		this.proteinMatches = proteinMatches;
	}

	/**
	 * @return the reportedPeptideString
	 */
	public String getReportedPeptideString() {
		return reportedPeptideString;
	}
	/**
	 * @param reportedPeptideString the reportedPeptideString to set
	 */
	public void setReportedPeptideString(String reportedPeptideString) {
		this.reportedPeptideString = reportedPeptideString;
	}
	/**
	 * @return the nakedPeptide
	 */
	public String getNakedPeptide() {
		return nakedPeptide;
	}
	/**
	 * @param nakedPeptide the nakedPeptide to set
	 */
	public void setNakedPeptide(String nakedPeptide) {
		this.nakedPeptide = nakedPeptide;
	}
	/**
	 * @return the mods
	 */
	public Map<Integer, BigDecimal> getMods() {
		return mods;
	}
	/**
	 * @param mods the mods to set
	 */
	public void setMods(Map<Integer, BigDecimal> mods) {
		this.mods = mods;
	}

}
