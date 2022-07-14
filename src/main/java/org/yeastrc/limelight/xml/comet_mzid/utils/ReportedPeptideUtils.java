package org.yeastrc.limelight.xml.comet_mzid.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class ReportedPeptideUtils {

    /**
     * Get the string representation of a reported peptide in the form of: PEP[21.2933]TIDE
     * @param peptideSequence
     * @param mods
     * @return
     */
    public static String getReportedPeptideString(String peptideSequence, Map<Integer, BigDecimal> mods ) {

        if( mods == null || mods.size() < 1 )
            return peptideSequence;

        StringBuilder sb = new StringBuilder();

        // n-terminal mod
        if(mods.containsKey(0)) {
            BigDecimal mass = mods.get(0);

            sb.append( "n[" );
            sb.append( mass.setScale( 2, RoundingMode.HALF_UP ).toString() );
            sb.append( "]" );
        }

        for (int i = 0; i < peptideSequence.length(); i++){
            String r = String.valueOf( peptideSequence.charAt(i) );
            sb.append( r );

            if( mods.containsKey( i + 1 ) ) {

                BigDecimal mass = mods.get( i + 1 );

                sb.append( "[" );
                sb.append( mass.setScale( 2, RoundingMode.HALF_UP ).toString() );
                sb.append( "]" );

            }
        }

        // c-terminal mod
        if(mods.containsKey(peptideSequence.length() + 1)) {
            BigDecimal mass = mods.get(peptideSequence.length() + 1);

            sb.append( "c[" );
            sb.append( mass.setScale( 2, RoundingMode.HALF_UP ).toString() );
            sb.append( "]" );
        }

        return sb.toString();
    }

}
