package com.miklosova.rdftocsvw.models;

import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelSheet;
import lombok.Data;

@ExcelSheet("test-files")
@Data
public class FilesParameters {

    @ExcelCellName("testName")
    public String testName;

    @ExcelCellName("filePath")
    public String filePath;

    @ExcelCellName("outputPath")
    public String outputPath;

}
