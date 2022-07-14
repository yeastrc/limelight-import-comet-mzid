package org.yeastrc.limelight.xml.comet_mzid.builder;

import org.yeastrc.limelight.limelight_import.api.xml_dto.*;
import org.yeastrc.limelight.limelight_import.api.xml_dto.SearchProgram.PsmAnnotationTypes;
import org.yeastrc.limelight.limelight_import.create_import_file_from_java_objects.main.CreateImportFileFromJavaObjectsMain;
import org.yeastrc.limelight.xml.comet_mzid.annotation.PSMAnnotationTypeSortOrder;
import org.yeastrc.limelight.xml.comet_mzid.annotation.PSMAnnotationTypes;
import org.yeastrc.limelight.xml.comet_mzid.annotation.PSMDefaultVisibleAnnotationTypes;
import org.yeastrc.limelight.xml.comet_mzid.constants.Constants;
import org.yeastrc.limelight.xml.comet_mzid.objects.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Map;

public class XMLBuilder {

	public void buildAndSaveXML(ConversionParameters conversionParameters,
								CometResults results)
    throws Exception {

		LimelightInput limelightInputRoot = new LimelightInput();

		limelightInputRoot.setFastaFilename( results.getSearchDatabase() );

		// add in the conversion program (this program) information
		ConversionProgramBuilder.createInstance().buildConversionProgramSection( limelightInputRoot, conversionParameters);

		SearchProgramInfo searchProgramInfo = new SearchProgramInfo();
		limelightInputRoot.setSearchProgramInfo( searchProgramInfo );

		SearchPrograms searchPrograms = new SearchPrograms();
		searchProgramInfo.setSearchPrograms( searchPrograms );

		{
			SearchProgram searchProgram = new SearchProgram();
			searchPrograms.getSearchProgram().add( searchProgram );

			searchProgram.setName( Constants.PROGRAM_NAME );
			searchProgram.setDisplayName( Constants.PROGRAM_NAME );
			searchProgram.setVersion(results.getVersion());


			//
			// Define the annotation types present in tide data
			//
			PsmAnnotationTypes psmAnnotationTypes = new PsmAnnotationTypes();
			searchProgram.setPsmAnnotationTypes( psmAnnotationTypes );

			FilterablePsmAnnotationTypes filterablePsmAnnotationTypes = new FilterablePsmAnnotationTypes();
			psmAnnotationTypes.setFilterablePsmAnnotationTypes( filterablePsmAnnotationTypes );

			for( FilterablePsmAnnotationType annoType : PSMAnnotationTypes.getFilterablePsmAnnotationTypes() ) {
				filterablePsmAnnotationTypes.getFilterablePsmAnnotationType().add( annoType );
			}
		}

		//
		// Define which annotation types are visible by default
		//
		DefaultVisibleAnnotations xmlDefaultVisibleAnnotations = new DefaultVisibleAnnotations();
		searchProgramInfo.setDefaultVisibleAnnotations( xmlDefaultVisibleAnnotations );

		VisiblePsmAnnotations xmlVisiblePsmAnnotations = new VisiblePsmAnnotations();
		xmlDefaultVisibleAnnotations.setVisiblePsmAnnotations( xmlVisiblePsmAnnotations );

		for( SearchAnnotation sa : PSMDefaultVisibleAnnotationTypes.getDefaultVisibleAnnotationTypes() ) {
			xmlVisiblePsmAnnotations.getSearchAnnotation().add( sa );
		}

		//
		// Define the default display order in proxl
		//
		AnnotationSortOrder xmlAnnotationSortOrder = new AnnotationSortOrder();
		searchProgramInfo.setAnnotationSortOrder( xmlAnnotationSortOrder );

		PsmAnnotationSortOrder xmlPsmAnnotationSortOrder = new PsmAnnotationSortOrder();
		xmlAnnotationSortOrder.setPsmAnnotationSortOrder( xmlPsmAnnotationSortOrder );

		for( SearchAnnotation xmlSearchAnnotation : PSMAnnotationTypeSortOrder.getPSMAnnotationTypeSortOrder() ) {
			xmlPsmAnnotationSortOrder.getSearchAnnotation().add( xmlSearchAnnotation );
		}

		//
		// Define the static mods
		//
		Map<String, BigDecimal> staticMods = results.getStaticMods();
		if(staticMods.size() > 0) {

			StaticModifications smods = new StaticModifications();
			limelightInputRoot.setStaticModifications( smods );

			for( String residue : staticMods.keySet() ) {

				StaticModification xmlSmod = new StaticModification();
				xmlSmod.setAminoAcid( residue );
				xmlSmod.setMassChange( staticMods.get(residue) );

				smods.getStaticModification().add( xmlSmod );
			}
		}

		//
		// Build MatchedProteins section and get map of protein names to MatchedProtein ids
		//
		MatchedProteinsBuilder.getInstance().buildMatchedProteins(
				limelightInputRoot,
				results.getProteinsSequenceProteinMap()
		);


		//
		// Define the peptide and PSM data
		//
		ReportedPeptides reportedPeptides = new ReportedPeptides();
		limelightInputRoot.setReportedPeptides( reportedPeptides );

		// iterate over each distinct reported peptide
		for( MzidReportedPeptide mzidReportedPeptide : results.getPeptidePSMMap().keySet() ) {

			// skip this if it only contains decoys
			if(!peptideHasProteins(mzidReportedPeptide, results)) {
				continue;
			}

			String reportedPeptideString = mzidReportedPeptide.getReportedPeptideString();

			ReportedPeptide xmlReportedPeptide = new ReportedPeptide();
			reportedPeptides.getReportedPeptide().add( xmlReportedPeptide );

			xmlReportedPeptide.setReportedPeptideString( reportedPeptideString );
			xmlReportedPeptide.setSequence( mzidReportedPeptide.getNakedPeptide() );

			MatchedProteinsForPeptide xProteinsForPeptide = new MatchedProteinsForPeptide();
			xmlReportedPeptide.setMatchedProteinsForPeptide( xProteinsForPeptide );

			// add in protein inference info
			int proteinCount = 0;
			for( String proteinId : mzidReportedPeptide.getProteinMatches() ) {

				if(results.getProteinsIdSequenceMap().containsKey( proteinId ) ) {
					proteinCount++;

					MzidProtein matchedProtein = results.getProteinsSequenceProteinMap().get(results.getProteinsIdSequenceMap().get(proteinId));
					int matchedProteinId = matchedProtein.getUniqueId();

					MatchedProteinForPeptide xProteinForPeptide = new MatchedProteinForPeptide();
					xProteinsForPeptide.getMatchedProteinForPeptide().add(xProteinForPeptide);

					xProteinForPeptide.setId(BigInteger.valueOf(matchedProteinId));
				}
			}

			if( proteinCount == 0) {
				throw new Exception("Could not find a protein for peptide: " + mzidReportedPeptide);
			}

			// add in the mods for this peptide
			if( mzidReportedPeptide.getMods() != null && mzidReportedPeptide.getMods().keySet().size() > 0 ) {

				PeptideModifications xmlModifications = new PeptideModifications();
				xmlReportedPeptide.setPeptideModifications( xmlModifications );

				for( int position : mzidReportedPeptide.getMods().keySet() ) {

					PeptideModification xmlModification = new PeptideModification();
					xmlModifications.getPeptideModification().add( xmlModification );

					xmlModification.setMass( mzidReportedPeptide.getMods().get( position ) );

					if( position == 0) {

						xmlModification.setIsNTerminal( true );

					} else if( position == mzidReportedPeptide.getNakedPeptide().length() + 1 ) {

						xmlModification.setIsCTerminal( true );

					} else {
						xmlModification.setPosition( BigInteger.valueOf( position ) );
					}
				}
			}


			// add in the PSMs and annotations
			Psms xmlPsms = new Psms();
			xmlReportedPeptide.setPsms( xmlPsms );

			// iterate over all PSMs for this reported peptide

			for( CometPSM psm : results.getPeptidePSMMap().get(mzidReportedPeptide) ) {

				Psm xmlPsm = new Psm();
				xmlPsms.getPsm().add( xmlPsm );

				xmlPsm.setScanNumber( new BigInteger( String.valueOf( psm.getScanNumber() ) ) );
				xmlPsm.setPrecursorCharge( new BigInteger( String.valueOf( psm.getCharge() ) ) );
				xmlPsm.setPrecursorMZ(psm.getObservedMoverZ());
				xmlPsm.setPrecursorRetentionTime(psm.getRetentionTimeSeconds());

				// add in the filterable PSM annotations (e.g., score)
				FilterablePsmAnnotations xmlFilterablePsmAnnotations = new FilterablePsmAnnotations();
				xmlPsm.setFilterablePsmAnnotations( xmlFilterablePsmAnnotations );

				if(conversionParameters.isOpenMod() && psm.getMassDiff() != null) {
					PsmOpenModification xmlPsmOpenModification = new PsmOpenModification();
					xmlPsm.setPsmOpenModification(xmlPsmOpenModification);
					xmlPsmOpenModification.setMass(psm.getMassDiff());
				}

				// handle psm scores

				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );

					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.COMET_ANNOTATION_TYPE_MASSDIFF );
					xmlFilterablePsmAnnotation.setSearchProgram( Constants.PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( psm.getMassDiff() );
				}

				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );

					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.COMET_ANNOTATION_TYPE_RANK );
					xmlFilterablePsmAnnotation.setSearchProgram( Constants.PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( BigDecimal.valueOf(psm.getRank()).setScale(0, RoundingMode.HALF_UP) );
				}

				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );

					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.COMET_ANNOTATION_TYPE_XCORR );
					xmlFilterablePsmAnnotation.setSearchProgram( Constants.PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( psm.getxCorr() );
				}

				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );

					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.COMET_ANNOTATION_TYPE_DELTACN );
					xmlFilterablePsmAnnotation.setSearchProgram( Constants.PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( psm.getDeltaCn() );
				}

				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );

					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.COMET_ANNOTATION_TYPE_EXPECT );
					xmlFilterablePsmAnnotation.setSearchProgram( Constants.PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( psm.geteValue() );
				}

				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );

					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.COMET_ANNOTATION_TYPE_NUMBER_UNMATCHED_PEAKS );
					xmlFilterablePsmAnnotation.setSearchProgram( Constants.PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( BigDecimal.valueOf(psm.getUnmatchedPeaks()).setScale(0, RoundingMode.HALF_UP) );
				}

				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );

					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.COMET_ANNOTATION_TYPE_NUMBER_MATCHED_PEAKS );
					xmlFilterablePsmAnnotation.setSearchProgram( Constants.PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( BigDecimal.valueOf(psm.getMatchedPeaks()).setScale(0, RoundingMode.HALF_UP) );
				}

				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );

					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.COMET_ANNOTATION_TYPE_SPRANK );
					xmlFilterablePsmAnnotation.setSearchProgram( Constants.PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( psm.getSpRank());
				}

				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );

					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.COMET_ANNOTATION_TYPE_SPSCORE );
					xmlFilterablePsmAnnotation.setSearchProgram( Constants.PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( psm.getSpScore());
				}

			}// end iterating over psms for a reported peptide

		}//end iterating over reported peptides


		// add in the config file(s)
		if(conversionParameters.getCometParamsFile() != null) {
			ConfigurationFiles xmlConfigurationFiles = new ConfigurationFiles();
			limelightInputRoot.setConfigurationFiles(xmlConfigurationFiles);

			ConfigurationFile xmlConfigurationFile = new ConfigurationFile();
			xmlConfigurationFiles.getConfigurationFile().add(xmlConfigurationFile);

			xmlConfigurationFile.setSearchProgram(Constants.PROGRAM_NAME);
			xmlConfigurationFile.setFileName(conversionParameters.getCometParamsFile().getName());
			xmlConfigurationFile.setFileContent(Files.readAllBytes(FileSystems.getDefault().getPath(conversionParameters.getCometParamsFile().getAbsolutePath())));
		}

		//make the xml file
		CreateImportFileFromJavaObjectsMain.getInstance().createImportFileFromJavaObjectsMain( new File(conversionParameters.getOutputFilePath() ), limelightInputRoot);

	}

	private boolean peptideHasProteins(MzidReportedPeptide mzidReportedPeptide, CometResults results) {

		for( String proteinId : mzidReportedPeptide.getProteinMatches() ) {
			if(results.getProteinsIdSequenceMap().containsKey( proteinId ) ) {
				return true;
			}
		}

		return false;
	}
	
}
