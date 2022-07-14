package org.yeastrc.limelight.xml.comet_mzid.objects;

import java.math.BigDecimal;
import java.util.Map;

public class CometPSM {

	public BigDecimal getxCorr() {
		return xCorr;
	}

	public void setxCorr(BigDecimal xCorr) {
		this.xCorr = xCorr;
	}

	public BigDecimal getDeltaCn() {
		return deltaCn;
	}

	public void setDeltaCn(BigDecimal deltaCn) {
		this.deltaCn = deltaCn;
	}

	public BigDecimal getSpScore() {
		return spScore;
	}

	public void setSpScore(BigDecimal spScore) {
		this.spScore = spScore;
	}

	public BigDecimal getSpRank() {
		return spRank;
	}

	public void setSpRank(BigDecimal spRank) {
		this.spRank = spRank;
	}

	public BigDecimal geteValue() {
		return eValue;
	}

	public void seteValue(BigDecimal eValue) {
		this.eValue = eValue;
	}

	public BigDecimal getRetentionTimeSeconds() {
		return retentionTimeSeconds;
	}

	public void setRetentionTimeSeconds(BigDecimal retentionTimeSeconds) {
		this.retentionTimeSeconds = retentionTimeSeconds;
	}

	public int getScanNumber() {
		return scanNumber;
	}

	public void setScanNumber(int scanNumber) {
		this.scanNumber = scanNumber;
	}

	public BigDecimal getObservedMoverZ() {
		return observedMoverZ;
	}

	public void setObservedMoverZ(BigDecimal observedMoverZ) {
		this.observedMoverZ = observedMoverZ;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public Map<Integer, BigDecimal> getModifications() {
		return modifications;
	}

	public void setModifications(Map<Integer, BigDecimal> modifications) {
		this.modifications = modifications;
	}

	public boolean isDecoy() {
		return isDecoy;
	}

	public void setDecoy(boolean decoy) {
		isDecoy = decoy;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public BigDecimal getMassDiff() {
		return massDiff;
	}

	public void setMassDiff(BigDecimal massDiff) {
		this.massDiff = massDiff;
	}

	public int getMatchedPeaks() {
		return matchedPeaks;
	}

	public void setMatchedPeaks(int matchedPeaks) {
		this.matchedPeaks = matchedPeaks;
	}

	public int getUnmatchedPeaks() {
		return unmatchedPeaks;
	}

	public void setUnmatchedPeaks(int unmatchedPeaks) {
		this.unmatchedPeaks = unmatchedPeaks;
	}

	private BigDecimal xCorr;
	private BigDecimal deltaCn;
	private BigDecimal spScore;
	private BigDecimal spRank;
	private BigDecimal eValue;
	private BigDecimal retentionTimeSeconds;
	private BigDecimal massDiff;
	private int matchedPeaks;
	private int unmatchedPeaks;
	private int scanNumber;
	private BigDecimal observedMoverZ;
	private int charge;
	private Map<Integer,BigDecimal> modifications;
	private boolean isDecoy;
	private int rank;

}
