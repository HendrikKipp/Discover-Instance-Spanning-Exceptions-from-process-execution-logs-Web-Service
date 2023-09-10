package org.ISEWebService.Model.DTO;

import org.ISEWebService.Model.Enums.ISEAlgorithmStatus;
import org.ISEWebService.Model.Enums.ISEAlgorithmType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Result {
    private String mergeAttribute;
    private Map<ISEAlgorithmType, ISEAlgorithmStatus> iseAlgorithmStatusMap = new HashMap<>();
    private Map<ISEAlgorithmType, ISECollective> iseAlgorithmResult = new HashMap<>();
    private Map<String, List<String>> processPPI = new HashMap<>();
    private Map<String, List<String>> processPPINames = new HashMap<>();

    private double waitThreshold;

    public Result(String mergeAttribute, boolean algorithmWait, boolean algorithmCancel, boolean algorithmRedo, boolean algorithmChange, boolean algorithmRework, double waitThreshold){
        this.mergeAttribute = mergeAttribute;

        if(algorithmWait){
            iseAlgorithmStatusMap.put(ISEAlgorithmType.WAIT, ISEAlgorithmStatus.REQUESTED);
        }else{
            iseAlgorithmStatusMap.put(ISEAlgorithmType.WAIT, ISEAlgorithmStatus.NOT_REQUESTED);
        }

        if(algorithmCancel){
            iseAlgorithmStatusMap.put(ISEAlgorithmType.CANCEL, ISEAlgorithmStatus.REQUESTED);
        }else{
            iseAlgorithmStatusMap.put(ISEAlgorithmType.CANCEL, ISEAlgorithmStatus.NOT_REQUESTED);
        }

        if(algorithmRedo){
            iseAlgorithmStatusMap.put(ISEAlgorithmType.REDO, ISEAlgorithmStatus.REQUESTED);
        }else{
            iseAlgorithmStatusMap.put(ISEAlgorithmType.REDO, ISEAlgorithmStatus.NOT_REQUESTED);
        }

        if(algorithmChange){
            iseAlgorithmStatusMap.put(ISEAlgorithmType.CHANGE, ISEAlgorithmStatus.REQUESTED);
        }else{
            iseAlgorithmStatusMap.put(ISEAlgorithmType.CHANGE, ISEAlgorithmStatus.NOT_REQUESTED);
        }

        if(algorithmRework){
            iseAlgorithmStatusMap.put(ISEAlgorithmType.REWORK, ISEAlgorithmStatus.REQUESTED);
        }else{
            iseAlgorithmStatusMap.put(ISEAlgorithmType.REWORK, ISEAlgorithmStatus.NOT_REQUESTED);
        }

        this.waitThreshold = waitThreshold;
    }

    public Map<ISEAlgorithmType, ISECollective> getIseAlgorithmResult() {
        return iseAlgorithmResult;
    }

    public Map<ISEAlgorithmType, ISEAlgorithmStatus> getIseAlgorithmStatusMap(){
        return this.iseAlgorithmStatusMap;
    }

    public void setMergeAttribute(String mergeAttribute){
        this.mergeAttribute = mergeAttribute;
    }

    public ISEAlgorithmStatus getISEAlgorithmStatus(ISEAlgorithmType algorithmType){
        return this.iseAlgorithmStatusMap.get(algorithmType);
    }

    public void setISEAlgorithmStatus(ISEAlgorithmType iseAlgorithmType, ISEAlgorithmStatus iseAlgorithmStatus){
        this.iseAlgorithmStatusMap.put(iseAlgorithmType, iseAlgorithmStatus);
    }

    public void setISEAlgorithmResult(ISEAlgorithmType algorithmType, ISECollective iseCollective){
        this.iseAlgorithmResult.put(algorithmType, iseCollective);
    }

    public String getMergeAttribute(){
        return this.mergeAttribute;
    }

    public double getWaitThreshold(){
        return waitThreshold;
    }

    public void addProcessPPI(String processName, List<String> ppi){
        this.processPPI.put(processName, ppi);
    }

    public void addProcessPPIName(String processName, List<String> ppiNames){
        this.processPPINames.put(processName, ppiNames);
    }

    public Map<String, List<String>> getProcessPPI() {
        return processPPI;
    }

    public Map<String, List<String>> getProcessPPINames() {
        return processPPINames;
    }
}