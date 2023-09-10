package org.ISEWebService.Model.EventLog;

import java.util.HashMap;
import java.util.List;

public class Log {
    private List<Trace> traces;
    private HashMap<String, Double> standardDeviation = new HashMap<>();
    private HashMap<String, Double> mean = new HashMap<>();
    private String processName;

    public Log(List<Trace> traces){
        this.traces = traces;
    }

    public List<Trace> getTraces() {
        return traces;
    }

    public double getStandardDeviation(String conceptName) {
        return standardDeviation.get(conceptName);
    }

    public void setStandardDeviation(String conceptName, double standardDeviation) {
        this.standardDeviation.put(conceptName, standardDeviation);
    }

    public double getMean(String conceptName) {
        return mean.get(conceptName);
    }

    public void setMean(String conceptName, double mean) {
        this.mean.put(conceptName, mean);
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessName() {
        return processName;
    }
}
