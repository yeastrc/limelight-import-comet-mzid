package org.yeastrc.limelight.xml.comet_mzid.objects;

import java.io.File;
import java.util.List;

public class ConversionParameters {
    public ConversionParameters(File mzidFile, String outputFilePath, ConversionProgramInfo conversionProgramInfo, File cometParamsFile, boolean isOpenMod) {
        this.mzidFile = mzidFile;
        this.outputFilePath = outputFilePath;
        this.conversionProgramInfo = conversionProgramInfo;
        this.cometParamsFile = cometParamsFile;
        this.isOpenMod = isOpenMod;
    }

    public File getMzidFile() {
        return mzidFile;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public ConversionProgramInfo getConversionProgramInfo() {
        return conversionProgramInfo;
    }

    public File getCometParamsFile() {
        return cometParamsFile;
    }

    public boolean isOpenMod() {
        return isOpenMod;
    }

    private File mzidFile;
    private String outputFilePath;
    private ConversionProgramInfo conversionProgramInfo;
    private File cometParamsFile;
    boolean isOpenMod;

}
