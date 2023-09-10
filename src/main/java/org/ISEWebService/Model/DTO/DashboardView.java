package org.ISEWebService.Model.DTO;

import java.util.ArrayList;
import java.util.List;

public class DashboardView {
    List<ProcessTab> processTab = new ArrayList<>();
    List<ISETab> iseTab = new ArrayList<>();

    public DashboardView(List<ProcessTab> processTab, List<ISETab> iseTab){
        this.processTab = processTab;
        this.iseTab = iseTab;
    }

    public List<ISETab> getIseTab() {
        return iseTab;
    }

    public List<ProcessTab> getProcessTab() {
        return processTab;
    }

    public void setIseTab(List<ISETab> iseTab) {
        this.iseTab = iseTab;
    }

    public void setProcessTab(List<ProcessTab> processTab) {
        this.processTab = processTab;
    }

    public class ProcessTab {
        String processName;
        List<String[]> ppiValues;

        public List<String[]> getPpiValues() {
            return ppiValues;
        }

        public String getProcessName() {
            return processName;
        }

        public ProcessTab(String processName, List<String[]> ppiValues){
            this.processName = processName;
            this.ppiValues = ppiValues;
        }
    }

    public class ISETab {
        String iseType;
        List<String[]> ppiValues;
        List<ISE> iseList;
        boolean isRequestedButNotApplied;

        public List<String[]> getPpiValues() {
            return ppiValues;
        }

        public List<ISE> getIseList() {
            return iseList;
        }

        public String getIseType() {
            return iseType;
        }

        public ISETab(String iseType, List<String[]> ppiValues, List<ISE> iseList, boolean isRequestedButNotApplied){
            this.iseType = iseType;
            this.ppiValues = ppiValues;
            this.iseList = iseList;
            this.isRequestedButNotApplied = isRequestedButNotApplied;
        }

        public boolean isRequestedButNotApplied() {
            return isRequestedButNotApplied;
        }
    }

    public class ISE{
        List<String[]> ppiValues;
        Long modelId;

        public List<String[]> getPpiValues() {
            return ppiValues;
        }

        public Long getModelId() {
            return modelId;
        }

        public ISE(List<String[]> ppiValues, Long modelId){
            this.ppiValues = ppiValues;
            this.modelId = modelId;
        }
    }
}
