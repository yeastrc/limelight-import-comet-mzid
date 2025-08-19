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

package org.yeastrc.limelight.xml.comet_mzid.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.yeastrc.limelight.xml.comet_mzid.constants.Constants;
import org.yeastrc.limelight.xml.comet_mzid.objects.ConversionParameters;
import org.yeastrc.limelight.xml.comet_mzid.objects.ConversionProgramInfo;
import org.yeastrc.limelight.xml.comet_mzid.utils.Limelight_GetVersion_FromFile_SetInBuildFromEnvironmentVariable;

import picocli.CommandLine;

@CommandLine.Command(name = "java -jar " + Constants.CONVERSION_PROGRAM_NAME,
		mixinStandardHelpOptions = true,
		versionProvider = LimelightConverterVersionProvider.class,
		sortOptions = false,
		synopsisHeading = "%n",
		descriptionHeading = "%n@|bold,underline Description:|@%n%n",
		optionListHeading = "%n@|bold,underline Options:|@%n",
		description = "Convert the results of a Comet analysis to a Limelight XML file suitable for import into Limelight.\n\n" +
				"More info at: " + Constants.CONVERSION_PROGRAM_URI
)
public class MainProgram implements Runnable {

	@CommandLine.Option(names = { "-m", "--mzid" }, required = true, description = "Full path to the location of the mzIdentML file (.mzid).")
	private File mzidFile;

	@CommandLine.Option(names = { "--open-mod" }, required = false, description = "If present, the mass different between observed and calculated mass will be reported as an open mod.")
	private boolean openMod;

	@CommandLine.Option(names = { "-o", "--out-file" }, required = true, description = "Full path to use for the Limelight XML output file (including file name).")
	private String outFile;

	@CommandLine.Option(names = { "-c", "--comet-params" }, required = false, description = "[Optional] Specify the path to the comet params file.")
	private File cometParamsFile;

	@CommandLine.Option(names = { "-v", "--verbose" }, required = false, description = "If this parameter is present, error messages will include a full stacktrace. Helpful for debugging.")
	private boolean verboseRequested = false;

	private String[] args;

	public void run() {

		printRuntimeInfo();

		if( !mzidFile.exists() ) {
			System.err.println( "Could not find pFind directory: " + mzidFile.getAbsolutePath() );
			System.exit( 1 );
		}

		if( cometParamsFile != null && !cometParamsFile.exists() ) {
			System.err.println( "Could not find comet params file: " + cometParamsFile.getAbsolutePath() );
			System.exit( 1 );
		}

		ConversionProgramInfo cpi = null;
		
		try {
			cpi = ConversionProgramInfo.createInstance( String.join( " ",  args ) );        
		} catch(Throwable t) {

			System.err.println("Error running conversion: " + t.getMessage());

			if(verboseRequested) {
				t.printStackTrace();
			}

			System.exit(1);
		}

		ConversionParameters cp = new ConversionParameters(mzidFile, outFile, cpi, cometParamsFile, openMod);

		try {
			ConverterRunner.createInstance().convertToLimelightXML(cp);
		} catch( Throwable t ) {

			if(verboseRequested) {
				t.printStackTrace();
			}

			System.err.println( "Encountered error during conversion: " + t.getMessage() );
			System.exit( 1 );
		}

		System.exit( 0 );
	}

	public static void main( String[] args ) {

		MainProgram mp = new MainProgram();
		mp.args = args;

		CommandLine.run(mp, args);
	}

	/**
	 * Print runtime info to STD ERR
	 * @throws Exception 
	 */
	public static void printRuntimeInfo() {

		try( BufferedReader br = new BufferedReader( new InputStreamReader( MainProgram.class.getResourceAsStream( "run.txt" ) ) ) ) {

			String line = null;
			while ( ( line = br.readLine() ) != null ) {

				line = line.replace( "{{URL}}", Constants.CONVERSION_PROGRAM_URI );
				line = line.replace( "{{VERSION}}", Limelight_GetVersion_FromFile_SetInBuildFromEnvironmentVariable.getVersion_FromFile_SetInBuildFromEnvironmentVariable() );

				System.err.println( line );
				
			}
			
			System.err.println( "" );

		} catch( Exception e) {
			;
		}
	}

}
