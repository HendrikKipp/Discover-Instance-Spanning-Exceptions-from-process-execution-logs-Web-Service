package org.ISEWebService.Service.PPICalculation;

import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Trace;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Date;

public class PPIProcess extends PPICalculation{
    /**
     * Process-PPI1: Process instances
     * Explanation: The number of process instances is calculated.
     * @param eventLog
     * @return
     */
    public String processPPI1(Log eventLog){
        return "" + eventLog.getTraces().size();
    }

    /**
     * Process-PPI2: Average process events
     * Explanation: The average number of process events is calculated.
     * @param eventLog
     * @return
     */
    public String processPPI2(Log eventLog){
        double totalProcessSteps = 0;
        for(Trace trace : eventLog.getTraces()){
            totalProcessSteps += trace.getEvents().size();
        }
        return "" + (int) (totalProcessSteps/eventLog.getTraces().size());
    }

    /**
     * Process-PPI3: Average throughput time
     * Explanation: The average throughput time of the process instances is calculated.
     * Process-PPI4: Median throughput time
     * Explanation: The median throughput time of the process instances is calculated.
     * @param eventLog
     * @return
     */
    public String[] processPPI3PPI4(Log eventLog){
        double[] throughputTimes = new double[eventLog.getTraces().size()];
        int amountTraces = eventLog.getTraces().size();
        for(int i=0; i<amountTraces; i++){
            Trace trace = eventLog.getTraces().get(i);
            Date startDate = trace.getEvents().get(0).getTimestamp();
            Date endDate = trace.getEvents().get(trace.getEvents().size() - 1).getTimestamp();
            throughputTimes[i] = endDate.getTime() - startDate.getTime();
        }
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(throughputTimes);

        double mean = descriptiveStatistics.getMean();
        double median = descriptiveStatistics.getPercentile(50);

        return new String[]{convertTimeToYearDayHourMinute(mean, true), convertTimeToYearDayHourMinute(median, true)};
    }

    /**
     * Process-PPI5: Time span
     * Explanation: The time period of the process instances is calculated.
     * @param eventLog
     * @return
     */
    public String processPPI5(Log eventLog){
        Date earliestDate = new Date(Long.MAX_VALUE);
        Date latestDate = new Date(0);
        for(Trace trace : eventLog.getTraces()){
            Date startDate = trace.getEvents().get(0).getTimestamp();
            Date endDate = trace.getEvents().get(trace.getEvents().size() - 1).getTimestamp();
            if(startDate.before(earliestDate)){
                earliestDate = startDate;
            }
            if(endDate.after(latestDate)){
                latestDate = endDate;
            }
        }

        double timePeriodeMilli = latestDate.getTime() - earliestDate.getTime();

        return convertTimeToYearDayHourMinute(timePeriodeMilli, false);
    }
}
