package org.ISEWebService.Model.DTO;

import java.util.ArrayList;
import java.util.List;

public class ISECollective {
    private final ISEClass iseClass;
    List<ISESingle> singleIseList = new ArrayList<>();
    private List<String> ppi = new ArrayList<>();
    private List<String> ppiNames = new ArrayList<>();

    public enum ISEClass{
        WAIT,
        CANCEL,
        REDO,
        CHANGE,
        REWORK
    }

    public ISECollective(ISEClass iseClass){
        this.iseClass = iseClass;
    }

    public List<ISESingle> getSingleIseList() {
        return singleIseList;
    }

    public void addISE(ISESingle singleIse){
        singleIseList.add(singleIse);
    }

    public void addPPI(String ppiValue){
        ppi.add(ppiValue);
    }

    public void addPPIName(String ppiName){
        ppiNames.add(ppiName);
    }

    public List<String> getPpi() {
        return ppi;
    }

    public List<String> getPpiNames() {
        return ppiNames;
    }
}
