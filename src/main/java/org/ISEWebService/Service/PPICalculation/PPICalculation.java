package org.ISEWebService.Service.PPICalculation;

import org.ISEWebService.Model.DTO.ISECollective;
import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Trace;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public abstract class PPICalculation {
    /**
     * Class-PPI1: ISE found
     * Explanation: The number of ISEs of the wait class found is calculated.
     * @param iseCollective
     */
    public String classPPI1(ISECollective iseCollective){
        return "" + iseCollective.getSingleIseList().size();
    }

    /**
     * Class-PPI2: Processes involved
     * Explanation: The number of processes involved in the ISE is calculated.
     * Wait-PPI3-General: Process instances involved
     * Explanation: The number of process instances involved in the ISE is calculated.
     * @param iseCollective
     */
    public String[] classPPI2PPI3(ISECollective iseCollective){
        Set<Log> processLogs = new HashSet<>();
        Set<Trace> processTraces = new HashSet<>();
        for(ISESingle iseSingle : iseCollective.getSingleIseList()){
            Log tempLog = iseSingle.getTrigger().getOriginalLog();
            Trace tempTrace = iseSingle.getTrigger().getOriginalTrace();

            if(!processLogs.contains(tempLog)){
                processLogs.add(tempLog);
            }

            if(!processTraces.contains(tempTrace)){
                processTraces.add(tempTrace);
            }

            for(Event handling : iseSingle.getHandling()){
                tempLog = handling.getOriginalLog();
                tempTrace = handling.getOriginalTrace();

                if(!processLogs.contains(tempLog)){
                    processLogs.add(tempLog);
                }

                if(!processTraces.contains(tempTrace)){
                    processTraces.add(tempTrace);
                }
            }
        }

        return new String[]{"" + processLogs.size(), "" + processTraces.size()};
    }

    /**
     * Class-PPI4: Sum of deviation of total throughput time
     * Explanation: For each process instance involved in an ISE, the deviation of the respective lead time from the
     * average lead time of the respective process is determined and then added up. The average is then formed on this.
     * @param iseCollective
     * @return
     */
    public String classPPI4(ISECollective iseCollective){
        double totalThroughputTime = 0;
        for(ISESingle iseSingle : iseCollective.getSingleIseList()){
            totalThroughputTime += iseSingle.getSumOfDeviationOfThroughputTime();
        }

        if(totalThroughputTime == 0){
            return "0 min";
        }else if(totalThroughputTime < 0){
            return "-" + convertTimeToYearDayHourMinute(totalThroughputTime, true);
        }
        return convertTimeToYearDayHourMinute(totalThroughputTime, true);
    }

    /**
     * ISE-PPI3: Average of deviations of throughput time
     * Explanation: For each process instance involved in the ISE, the deviation of the respective throughput time from
     * the average throughput time of the respective process is determined and then the deviations are added up.
     * @param iseSingle
     * @return
     */
    protected String isePPI3(ISESingle iseSingle){
        double sumDeviationsThroughputTime = 0;

        double triggerTraceThroughputTime = iseSingle.getTrigger().getOriginalTrace().getThroughputTime();

        double triggerAllTracesThroughputTimes = 0;
        int triggerCountTraces = 0;
        for(Trace trace : iseSingle.getTrigger().getOriginalLog().getTraces()){
            triggerAllTracesThroughputTimes += trace.getThroughputTime();
            triggerCountTraces++;
        }

        double triggerAverageThroughputTime = (triggerAllTracesThroughputTimes - triggerTraceThroughputTime) / (triggerCountTraces - 1);
        double triggerDeviationInThroughputTime = triggerTraceThroughputTime - triggerAverageThroughputTime;
        sumDeviationsThroughputTime += triggerDeviationInThroughputTime;

        for(Event handlingEvent : iseSingle.getHandling()){
            double handlingTraceThroughputTime = handlingEvent.getOriginalTrace().getThroughputTime();

            double handlingAllTracesThroughputTimes = 0;
            int handlingCountTraces = 0;
            for(Trace trace : handlingEvent.getOriginalLog().getTraces()){
                handlingAllTracesThroughputTimes += trace.getThroughputTime();
                handlingCountTraces++;
            }

            double handlingAverageThroughputTime = (handlingAllTracesThroughputTimes - handlingTraceThroughputTime) / (handlingCountTraces - 1);
            double handlingDeviationThroughputTime = handlingTraceThroughputTime - handlingAverageThroughputTime;
            sumDeviationsThroughputTime += handlingDeviationThroughputTime;
        }

        // Save value for general Class-PPI 4
        iseSingle.setSumOfDeviationOfThroughputTime(sumDeviationsThroughputTime);

        if(sumDeviationsThroughputTime == 0){
            return "0 min";
        }else if(sumDeviationsThroughputTime < 0){
            return "-" + convertTimeToYearDayHourMinute(((-1) * sumDeviationsThroughputTime)/(iseSingle.getHandling().size() + 1), true);
        }
        return convertTimeToYearDayHourMinute(sumDeviationsThroughputTime/(iseSingle.getHandling().size() + 1), true);
    }

    protected String convertTimeToYearDayHourMinute(double timeInMilli, boolean includeMinute){
        int timeYear = (int) Math.round(timeInMilli / (1000 * 60 * 60 * 24 * 365));
        int timeDays = (int) (Math.round(timeInMilli / (1000 * 60 * 60 * 24)) % 365);
        double timeHours = (timeInMilli / (1000 * 60 * 60)) % 24;
        double timeMinute = (timeInMilli / (1000 * 60)) % 60;

        String result = "";
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        if(timeYear > 0){
            result = result + timeYear + "y ";
        }
        if(timeDays > 0){
            result = result + timeDays + "d ";
        }
        if(includeMinute){
            if(((int) timeHours) > 0){
                result = result + (int) timeHours + "h ";
            }
            if(timeMinute > 0){
                result = result + decimalFormat.format(timeMinute) + "min";
            }
        }else{
            if(timeHours > 0){
                result = result + decimalFormat.format(timeHours) + "h";
            }
        }

        return result;
    }
}
