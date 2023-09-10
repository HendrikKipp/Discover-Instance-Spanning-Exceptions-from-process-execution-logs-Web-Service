package org.ISEWebService.Service.ISEAlgorithms;

import org.ISEWebService.Model.DTO.*;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Trace;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WaitAlgorithm {

    /**
     * Discover ISE of type WAIT
     * @param mergedTraces
     * @param originalLogs
     * @param th
     * @return
     */
    public ISECollective discoverWait(List<Trace> mergedTraces, List<Log> originalLogs, double th) {
        calculate_temporal_information(originalLogs);

        Event trigger = null;
        List<Event> handling = new ArrayList<>();

        ISECollective iseCollective = new ISECollective(ISECollective.ISEClass.WAIT);

        for (Trace trace : mergedTraces) {
            for (Event event : trace.getEvents()) {
                if (!event.getLifecycle().equals("complete")) {
                    continue;
                }
                if (trigger == null & outlier(this.calculateZScore(event), th)) {
                    trigger = event;
                    continue;
                }
                if (trigger != null & outlier(this.calculateZScore(event), th)) {
                    handling.add(event);
                } else if (trigger != null & !outlier(this.calculateZScore(event), th)) {
                    if (!handling.isEmpty()) {
                        iseCollective.addISE(new ISESingle(trigger, handling));
                    }
                    trigger = null;
                    handling = new ArrayList<>();
                }
            }
            if (trigger != null && !handling.isEmpty()) {
                iseCollective.addISE(new ISESingle(trigger, handling));

            }
            trigger = null;
            handling = new ArrayList<>();
        }

        return iseCollective;
    }

    /**
     * Calculates the temporal information that is necessary for the execution of the wait algorithm. First all
     * durations of a task are collected per process log. A task describes one or more activities with the same name.
     * Then the mean and the standard deviation are determined per process log for each task on the basis of the task
     * durations and stored in the process log.
     *
     * @param originalLogs The list of original, not merged event logs
     */
    private void calculate_temporal_information(List<Log> originalLogs) {
        for (Log log : originalLogs) {

            HashMap<String, List<Double>> tempSdMe = new HashMap<>();

            for (Trace trace : log.getTraces()) {

                for (Event event : trace.getEvents()) {
                    String lifecycleTransition = event.getLifecycle();
                    String conceptName = event.getConceptName();

                    if (lifecycleTransition.equals("complete")) {
                        if (tempSdMe.containsKey(conceptName)) {
                            tempSdMe.get(conceptName).add(event.getTask().getDurationTask());
                        } else {
                            List<Double> tempDoubleList = new ArrayList<>();
                            tempDoubleList.add(event.getTask().getDurationTask());
                            tempSdMe.put(conceptName, tempDoubleList);
                        }
                    }
                }
            }

            for (String s : tempSdMe.keySet()) {
                log.setMean(s, this.calculateMean(tempSdMe.get(s)));
                log.setStandardDeviation(s, this.calculateStandardDeviation(tempSdMe.get(s)));
            }
        }
    }

    /**
     * Calculates the standard deviation of the given list of double values using the Apache Commons Math 3.6.1 API.
     *
     * @param standardDeviationList List of double values
     * @return Standard deviation as double value
     */
    private double calculateStandardDeviation(List<Double> standardDeviationList) {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(standardDeviationList.stream().mapToDouble(Double::doubleValue).toArray());
        return descriptiveStatistics.getStandardDeviation();
    }

    /**
     * Calculates the mean of the given list of double values using the Apache Commons Math 3.6.1 API.
     *
     * @param meanList List of double values
     * @return Mean as double value
     */
    private double calculateMean(List<Double> meanList) {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(meanList.stream().mapToDouble(Double::doubleValue).toArray());
        return descriptiveStatistics.getMean();
    }

    /**
     * Determines whether it is an outlier based on the threshold and the zScore
     *
     * @param zScore Z-Score as double value
     * @param th     Wait threshold as double value
     * @return Outlier as boolean value
     */
    private boolean outlier(double zScore, double th) {
        return zScore > th;
    }

    /**
     * Calculates the zScore of the associated task (task consists of one or more activities) based on an event.
     *
     * @param event Event-Object
     * @return ZScore as double value
     */
    private double calculateZScore(Event event) {
        double X = event.getTask().getDurationTask();
        String conceptName = event.getConceptName();
        double mean = event.getOriginalLog().getMean(conceptName);
        double standardDeviation = event.getOriginalLog().getStandardDeviation(conceptName);

        return (X - mean) / standardDeviation;
    }
}
