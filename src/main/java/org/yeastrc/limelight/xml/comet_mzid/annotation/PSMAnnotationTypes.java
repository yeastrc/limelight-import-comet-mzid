/*
 * Original author: Michael Riffle <mriffle .at. uw.edu>
 *                  
 * Copyright 2018 University of Washington - Seattle, WA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yeastrc.limelight.xml.comet_mzid.annotation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.yeastrc.limelight.limelight_import.api.xml_dto.FilterDirectionType;
import org.yeastrc.limelight.limelight_import.api.xml_dto.FilterablePsmAnnotationType;


public class PSMAnnotationTypes {

	// comet scores
	public static final String COMET_ANNOTATION_TYPE_XCORR = "XCorr";
	public static final String COMET_ANNOTATION_TYPE_DELTACN = "DeltaCN";
	public static final String COMET_ANNOTATION_TYPE_SPSCORE = "Sp Score";
	public static final String COMET_ANNOTATION_TYPE_SPRANK = "Sp Rank";
	public static final String COMET_ANNOTATION_TYPE_EXPECT = "E-Value";
	public static final String COMET_ANNOTATION_TYPE_RANK = "Rank";
	public static final String COMET_ANNOTATION_TYPE_MASSDIFF = "Mass Diff";
	public static final String COMET_ANNOTATION_TYPE_NUMBER_MATCHED_PEAKS = "matched peaks";
	public static final String COMET_ANNOTATION_TYPE_NUMBER_UNMATCHED_PEAKS = "unmatched peaks";

	public static List<FilterablePsmAnnotationType> getFilterablePsmAnnotationTypes() {
		List<FilterablePsmAnnotationType> types = new ArrayList<FilterablePsmAnnotationType>();

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( COMET_ANNOTATION_TYPE_XCORR );
			type.setDescription( "Comet cross-correlation coefficient" );
			type.setFilterDirection( FilterDirectionType.ABOVE );

			types.add( type );
		}

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( COMET_ANNOTATION_TYPE_DELTACN );
			type.setDescription( "Difference between the XCorr of this PSM and the next best PSM (with a dissimilar peptide)" );
			type.setFilterDirection( FilterDirectionType.ABOVE );

			types.add( type );
		}

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( COMET_ANNOTATION_TYPE_SPSCORE );
			type.setDescription( "Score indicating how well theoretical and actual peaks matched." );
			type.setFilterDirection( FilterDirectionType.ABOVE );

			types.add( type );
		}

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( COMET_ANNOTATION_TYPE_SPRANK );
			type.setDescription( "The rank of this peptide match for this spectrum basedo n Sp Score" );
			type.setFilterDirection( FilterDirectionType.BELOW );

			types.add( type );
		}

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( COMET_ANNOTATION_TYPE_EXPECT );
			type.setDescription( "The e-value, or the estimation of the chance of observing a hit of this quality by chance." );
			type.setFilterDirection( FilterDirectionType.BELOW );
			type.setDefaultFilterValue( new BigDecimal( "0.01" ) );

			types.add( type );
		}

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( COMET_ANNOTATION_TYPE_RANK );
			type.setDescription( "The rank of this peptide match for this spectrum (1 is best)." );
			type.setFilterDirection( FilterDirectionType.BELOW );
			type.setDefaultFilterValue( new BigDecimal( "1" ) );

			types.add( type );
		}

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( COMET_ANNOTATION_TYPE_NUMBER_MATCHED_PEAKS );
			type.setDescription( "Number of matched peaks in the PSM." );
			type.setFilterDirection( FilterDirectionType.ABOVE );

			types.add( type );
		}

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( COMET_ANNOTATION_TYPE_NUMBER_UNMATCHED_PEAKS );
			type.setDescription( "Number of unmatched peaks in the PSM." );
			type.setFilterDirection( FilterDirectionType.BELOW );

			types.add( type );
		}

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( COMET_ANNOTATION_TYPE_MASSDIFF );
			type.setDescription( "Difference between observed mass and theoretical mass." );
			type.setFilterDirection( FilterDirectionType.BELOW );

			types.add( type );
		}

		return types;
	}
	
	
}
