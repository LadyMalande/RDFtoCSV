package com.miklosova.rdftocsvw.models;

import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelSheet;
import lombok.Data;

@ExcelSheet("rdf-dereferencer")
@Data
public class DereferencerTestParameters {

    @ExcelCellName("testName")
    public String testName;

    @ExcelCellName("url")
    public String url;

    @ExcelCellName("predicateURI")
    public String predicateURI;

    @ExcelCellName("predicateName")
    public String predicateName;
}
