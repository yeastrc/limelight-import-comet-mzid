package org.yeastrc.limelight.xml.comet_mzid.reader;

import info.psidev.psi.pi.mzidentml._1.*;
import org.yeastrc.limelight.xml.comet_mzid.objects.CometPSM;
import org.yeastrc.limelight.xml.comet_mzid.objects.MzidProtein;
import org.yeastrc.limelight.xml.comet_mzid.objects.MzidReportedPeptide;
import org.yeastrc.limelight.xml.comet_mzid.objects.CometResults;
import org.yeastrc.limelight.xml.comet_mzid.utils.ProteinUtils;
import org.yeastrc.limelight.xml.comet_mzid.utils.ReportedPeptideUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cometMzidResultsReader {

    public static CometResults getResults(File mzidFile) throws Exception {

        MzIdentMLType mzIdentML = getMzIdentML(mzidFile);

        String version = mzIdentML.getAnalysisSoftwareList().getAnalysisSoftware().get(0).getVersion();
        System.err.println("\tComet version: " + version);

        Map<String, BigDecimal> staticMods = getStaticMods(mzIdentML);
        System.err.println("\tFound " + staticMods.size() + " static mods.");

        // A map of peptides parsed from the mzIdentML, keyed by Peptide.id in that file
        Map<String, MzidReportedPeptide> reportedPeptideMap = getPeptides(mzIdentML, staticMods);
        System.err.println("\tFound " + reportedPeptideMap.size() + " distinct peptide ids.");

        System.err.print("\tReading PSMs... ");
        Map<MzidReportedPeptide, Collection<CometPSM>> psmPeptideMap = getPSMPeptideMap(mzIdentML, reportedPeptideMap);
        System.err.println("Done.");

        System.err.println("Done reading .mzid file.");

        CometResults results = new CometResults();
        results.setPeptidePSMMap( psmPeptideMap );
        results.setProteinsIdSequenceMap( getProteinsIdSequenceMap(mzIdentML) );
        results.setProteinsSequenceProteinMap( getProteinsSequenceProteinMap(mzIdentML) );
        results.setStaticMods( staticMods );
        results.setVersion( version );
        results.setSearchDatabase(getSearchDatabase(mzIdentML));

        return results;
    }

    private static String getSearchDatabase(MzIdentMLType mzIdentML) {

        String searchDatabase = "Unknown";

        try {

            searchDatabase = (new File(mzIdentML.getDataCollection().getInputs().getSearchDatabase().get(0).getLocation()).getName());

        } catch(Throwable t) {
            ;
        }

        return searchDatabase;
    }

    private static Map<MzidReportedPeptide, Collection<CometPSM>> getPSMPeptideMap(MzIdentMLType mzIdentML,
                                                                                   Map<String, MzidReportedPeptide> reportedPeptideMap) throws Exception {

        Map<MzidReportedPeptide, Collection<CometPSM>> psmPeptideMap = new HashMap<>();
        SpectrumIdentificationListType spectrumIdentificationList = getSpectrumIdentificationList(mzIdentML);

        for(SpectrumIdentificationResultType result : spectrumIdentificationList.getSpectrumIdentificationResult()) {
            int scanNumber = getScanNumberFromSpectrumID(result.getSpectrumID());
            BigDecimal retentionTime = BigDecimal.valueOf(getRetentionTimeFromResult(result));

            for(SpectrumIdentificationItemType item : result.getSpectrumIdentificationItem()) {

                MzidReportedPeptide reportedPeptide = reportedPeptideMap.get(item.getPeptideRef());

                // this PSM matches a peptide that didn't map to a target--it's a decoy
                if(reportedPeptide == null) {
                    continue;
                }

                int charge = item.getChargeState();
                int rank = item.getRank();
                BigDecimal obsMZ = BigDecimal.valueOf(item.getExperimentalMassToCharge());
                BigDecimal massDiff = BigDecimal.valueOf(getMassDiff(item.getExperimentalMassToCharge(), item.getCalculatedMassToCharge(), charge)).setScale(4, RoundingMode.HALF_UP);


                BigDecimal xcorr = null;
                BigDecimal deltacn = null;
                BigDecimal spscore = null;
                BigDecimal sprank = null;
                BigDecimal evalue = null;
                Integer matchedPeaks = null;
                Integer unMatchedPeaks = null;

                for( AbstractParamType cv : item.getParamGroup()) {
                    String name = cv.getName();

                    switch(name) {
                        case "number of matched peaks":
                            matchedPeaks = Integer.parseInt(cv.getValue());
                            break;
                        case "number of unmatched peaks":
                            unMatchedPeaks = Integer.parseInt(cv.getValue());
                            break;
                        case "Comet:xcorr":
                            xcorr = new BigDecimal(cv.getValue());
                            break;
                        case "Comet:deltacn":
                            deltacn = new BigDecimal(cv.getValue());
                            break;
                        case "Comet:spscore":
                            spscore = new BigDecimal(cv.getValue());
                            break;
                        case "Comet:sprank":
                            sprank = new BigDecimal(cv.getValue());
                            break;
                        case "Comet:expectation value":
                            evalue = new BigDecimal(cv.getValue());
                            break;
                    }
                }

                if(matchedPeaks == null) {
                    throw new Exception("Could not find matched peaks for PSM " + item.getId());
                }
                if(unMatchedPeaks == null) {
                    throw new Exception("Could not find unmatched peaks PSM " + item.getId());
                }
                if(xcorr == null) {
                    throw new Exception("Could not find xcorr PSM " + item.getId());
                }
                if(deltacn == null) {
                    throw new Exception("Could not find deltacn PSM " + item.getId());
                }
                if(spscore == null) {
                    throw new Exception("Could not find spscore PSM " + item.getId());
                }
                if(sprank == null) {
                    throw new Exception("Could not find sprank PSM " + item.getId());
                }
                if(evalue == null) {
                    throw new Exception("Could not find evalue PSM " + item.getId());
                }

                CometPSM psm = new CometPSM();
                psm.setCharge(charge);
                psm.setMassDiff(massDiff);
                psm.setDecoy(false);
                psm.setObservedMoverZ(obsMZ);
                psm.setRank(rank);
                psm.setScanNumber(scanNumber);
                psm.setRetentionTimeSeconds(retentionTime);
                psm.setxCorr(xcorr);
                psm.setDeltaCn(deltacn);
                psm.setSpScore(spscore);
                psm.setSpRank(sprank);
                psm.seteValue(evalue);
                psm.setMatchedPeaks(matchedPeaks);
                psm.setUnmatchedPeaks(unMatchedPeaks);

                if(!psmPeptideMap.containsKey(reportedPeptide)) {
                    psmPeptideMap.put(reportedPeptide, new HashSet<>());
                }

                psmPeptideMap.get(reportedPeptide).add(psm);
            }
        }


        return psmPeptideMap;
    }

    private static double getMassDiff(double observedMz, double expectedMz, int charge) {

        double neutralObservedMass = observedMz * charge;
        double neutralExpectedMass = expectedMz * charge;

        return neutralObservedMass - neutralExpectedMass;
    }

    private static double getRetentionTimeFromResult(SpectrumIdentificationResultType result) throws Exception {

        for(AbstractParamType param : result.getParamGroup()) {
            if (param.getName().equals("retention time")) {
                return Double.valueOf(param.getValue());
            }
        }

        throw new Exception("Could not get retention time.");
    }

    private static int getScanNumberFromSpectrumID(String spectrumID) throws Exception {
        Pattern p = Pattern.compile("^.*scan=(\\d+)$");
        Matcher m = p.matcher(spectrumID);

        if(!m.matches()) {
            throw new Exception("Could not parse scan number from " + spectrumID);
        }

        return Integer.parseInt(m.group(1));
    }

    private static SpectrumIdentificationListType getSpectrumIdentificationList(MzIdentMLType mzIdentML) throws Exception {

        DataCollectionType dataCollection = mzIdentML.getDataCollection();
        if(dataCollection == null) {
            throw new Exception("Could not find DataCollection element.");
        }

        AnalysisDataType analysisData = dataCollection.getAnalysisData();
        if(analysisData == null) {
            throw new Exception("Could not find AnalysisData element.");
        }

        SpectrumIdentificationListType spectrumIdentificationList = analysisData.getSpectrumIdentificationList().get(0);    // assume only one spectrum identification list
        if(spectrumIdentificationList == null) {
            throw new Exception("Could not find SpectrumIdentificationList element.");
        }

        return spectrumIdentificationList;
    }

    private static Map<String, MzidReportedPeptide> getPeptides(MzIdentMLType mzIdentML, Map<String, BigDecimal> staticMods) throws Exception {
        Map<String, MzidReportedPeptide> peptideMap = new HashMap<>();
        Map<String, Collection<String>> pepEvidenceMap = getPeptideEvidenceMap(mzIdentML);

        SequenceCollectionType sequenceCollection = getSequenceCollection(mzIdentML);
        for(PeptideType peptide : sequenceCollection.getPeptide()) {

            // this peptide didn't map to any non decoy proteins, skip it
            if(!pepEvidenceMap.containsKey(peptide.getId())) {
                continue;
            }

            MzidReportedPeptide mzidReportedPeptide = getReportedPeptide(peptide, pepEvidenceMap, staticMods);

            if(peptideMap.containsKey(peptide.getId())) {
                throw new Exception("Got two peptides with id: " + peptide.getId());
            }
            peptideMap.put(peptide.getId(), mzidReportedPeptide);
        }

        return peptideMap;
    }

    private static MzidReportedPeptide getReportedPeptide(PeptideType peptide, Map<String, Collection<String>> pepEvidenceMap, Map<String, BigDecimal> staticMods) {
        MzidReportedPeptide reportedPeptide = new MzidReportedPeptide();
        Map<Integer, BigDecimal> mods = getDynamicMods(peptide, staticMods);

        reportedPeptide.setNakedPeptide(peptide.getPeptideSequence());
        reportedPeptide.setMods(mods);
        reportedPeptide.setReportedPeptideString(ReportedPeptideUtils.getReportedPeptideString(peptide.getPeptideSequence(), mods));
        reportedPeptide.setProteinMatches(pepEvidenceMap.get(peptide.getId()));

        return reportedPeptide;
    }

    private static Map<Integer, BigDecimal> getDynamicMods(PeptideType peptide, Map<String, BigDecimal> staticMods) {
        Map<Integer, BigDecimal> mods = new HashMap<>();

        for(ModificationType mod : peptide.getModification()) {

            String peptideSequence = peptide.getPeptideSequence();
            int position = mod.getLocation();
            BigDecimal moddedMass = BigDecimal.valueOf(mod.getMonoisotopicMassDelta());

            String moddedResidue;

            if(position == 0) { moddedResidue = "n"; }
            else if(position == peptideSequence.length() + 1) { moddedResidue = "c"; }
            else { moddedResidue = peptideSequence.substring(position - 1, position); }

            if(!staticMods.containsKey(moddedResidue) || !bigDecimalsAreEqual(staticMods.get(moddedResidue), moddedMass, 3)) {
                mods.put(mod.getLocation(), BigDecimal.valueOf(mod.getMonoisotopicMassDelta()));
            }
        }

        return mods;
    }

    private static boolean bigDecimalsAreEqual(BigDecimal bd1, BigDecimal bd2, int scale) {
        return bd1.setScale(scale, RoundingMode.HALF_UP).equals(bd2.setScale(scale, RoundingMode.HALF_UP));
    }


    private static Map<String, Collection<String>> getPeptideEvidenceMap(MzIdentMLType mzIdentML) throws Exception {
        Map<String, Collection<String>> pepEvidenceMap = new HashMap<>();

        SequenceCollectionType sequenceCollection = getSequenceCollection(mzIdentML);
        for(PeptideEvidenceType peptideEvidence : sequenceCollection.getPeptideEvidence()) {
            // do not include decoys
            if(peptideEvidence.isIsDecoy()) {
                continue;
            }

            String pepRef = peptideEvidence.getPeptideRef();
            String protRef = peptideEvidence.getDBSequenceRef();

            if(!pepEvidenceMap.containsKey(pepRef)) {
                pepEvidenceMap.put(pepRef, new HashSet<>());
            }

            pepEvidenceMap.get(pepRef).add(protRef);
        }

        return pepEvidenceMap;
    }

    private static SequenceCollectionType getSequenceCollection(MzIdentMLType mzIdentML) throws Exception {
        SequenceCollectionType sequenceCollection = mzIdentML.getSequenceCollection();
        if( sequenceCollection == null ) {
            throw new Exception("Did not find SequenceCollection in .mzid file.");
        }

        return sequenceCollection;
    }


    private static Map<String, String> getProteinsIdSequenceMap(MzIdentMLType mzIdentML) throws Exception {
        Map<String, String> proteinMap = new HashMap<>();

        SequenceCollectionType sequenceCollection = getSequenceCollection(mzIdentML);

        for(DBSequenceType dbSequence : sequenceCollection.getDBSequence()) {
            String id = dbSequence.getId();
            String sequence = dbSequence.getSeq();

            if(sequence == null) {
                throw new Exception("Could not find sequence for protein: " + id);
            }

            if(!ProteinUtils.isIdDecoy(id)) {
                if(proteinMap.containsKey(id)) {
                    throw new Exception("Got two entries for protein id: " + id);
                }

                proteinMap.put(id, sequence);
            }
        }

        return proteinMap;
    }

    private static Map<String, MzidProtein> getProteinsSequenceProteinMap(MzIdentMLType mzIdentML) throws Exception {
        Map<String, MzidProtein> proteinMap = new HashMap<>();

        SequenceCollectionType sequenceCollection = getSequenceCollection(mzIdentML);
        int counter = 1;

        for(DBSequenceType dbSequence : sequenceCollection.getDBSequence()) {
            String name = dbSequence.getName();
            String accession = dbSequence.getAccession();
            String sequence = dbSequence.getSeq();

            // don't include decoys
            if(ProteinUtils.isNameDecoy(accession)) {
                continue;
            }

            MzidProtein.Annotation anno = new MzidProtein.Annotation();
            anno.setDescription( name );
            anno.setName( accession );

            if(!proteinMap.containsKey(sequence)) {
                proteinMap.put(sequence, new MzidProtein(sequence, counter));
                counter++;
            }

            MzidProtein mzidProtein = proteinMap.get(sequence);
            mzidProtein.getAnnotations().add(anno);
        }

        return proteinMap;
    }

    private static Map<String, BigDecimal> getStaticMods(MzIdentMLType mzIdentML) {
        Map<String, BigDecimal> staticMods = new HashMap<>();

        AnalysisProtocolCollectionType analysisProtocol = mzIdentML.getAnalysisProtocolCollection();
        if(analysisProtocol != null) {
            for( SpectrumIdentificationProtocolType sipt : analysisProtocol.getSpectrumIdentificationProtocol() ) {
                ModificationParamsType modificationParams = sipt.getModificationParams();
                if(modificationParams != null) {
                    for(SearchModificationType searchModification : modificationParams.getSearchModification()) {
                        if(searchModification.isFixedMod()) {
                            BigDecimal massShift = BigDecimal.valueOf( searchModification.getMassDelta() );

                            for(String residue : searchModification.getResidues()) {
                                staticMods.put(residue, massShift);
                            }
                        }
                    }
                }
            }
        }

        return staticMods;
    }

    private static MzIdentMLType getMzIdentML(File mzidFile) throws JAXBException {

        MzIdentMLType mzIdentML = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MzIdentMLType.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            mzIdentML = (MzIdentMLType)jaxbUnmarshaller.unmarshal( mzidFile );
        } catch (JAXBException e) {
            System.err.println("Error processing mzIdentML file: " + mzidFile.getAbsolutePath());
            throw e;
        }


        return mzIdentML;
    }
}
