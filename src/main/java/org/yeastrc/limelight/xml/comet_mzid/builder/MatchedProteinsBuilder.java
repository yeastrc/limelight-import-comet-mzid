package org.yeastrc.limelight.xml.comet_mzid.builder;

import java.math.BigInteger;
import java.util.Map;

import org.yeastrc.limelight.limelight_import.api.xml_dto.LimelightInput;
import org.yeastrc.limelight.limelight_import.api.xml_dto.MatchedProtein;
import org.yeastrc.limelight.limelight_import.api.xml_dto.MatchedProteinLabel;
import org.yeastrc.limelight.limelight_import.api.xml_dto.MatchedProteins;
import org.yeastrc.limelight.xml.comet_mzid.objects.MzidProtein;

/**
 * Build the MatchedProteins section of the limelight XML docs. This is done by finding all proteins in the FASTA
 * file that contains any of the peptide sequences found in the experiment. 
 * 
 * This is generalized enough to be usable by any pipeline
 * 
 * @author mriffle
 *
 */
public class MatchedProteinsBuilder {

	public static MatchedProteinsBuilder getInstance() { return new MatchedProteinsBuilder(); }


	public void buildMatchedProteins(LimelightInput limelightInputRoot, Map<String, MzidProtein> proteinSequenceProteinMap) throws Exception {
		
		System.err.print( " Matching peptides to proteins..." );

		MatchedProteins xmlMatchedProteins = new MatchedProteins();
		limelightInputRoot.setMatchedProteins( xmlMatchedProteins );

		for( String sequence : proteinSequenceProteinMap.keySet() ) {

			MzidProtein protein = proteinSequenceProteinMap.get(sequence);

			MatchedProtein xmlProtein = new MatchedProtein();
			xmlMatchedProteins.getMatchedProtein().add( xmlProtein );

			xmlProtein.setSequence( protein.getSequence() );
			xmlProtein.setId( BigInteger.valueOf(protein.getUniqueId()));

			for( MzidProtein.Annotation anno : protein.getAnnotations() ) {
				MatchedProteinLabel xmlMatchedProteinLabel = new MatchedProteinLabel();
				xmlProtein.getMatchedProteinLabel().add( xmlMatchedProteinLabel );

				xmlMatchedProteinLabel.setName( anno.getName() );

				if( anno.getDescription() != null )
					xmlMatchedProteinLabel.setDescription( anno.getDescription() );
			}
		}
	}
	
}
