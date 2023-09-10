package org.ISEWebService.Service;

import org.ISEWebService.Model.DTO.ISECollective;
import org.ISEWebService.Model.DTO.ISESingle;
import org.ISEWebService.Model.DTO.Result;
import org.ISEWebService.Model.EventLog.Event;
import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Trace;
import org.ISEWebService.Service.PPICalculation.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

@Service
public class PPIService {

    public void computeProcessPPI(List<Log> eventLogs, Result result){
        PPIProcess ppiProcess = new PPIProcess();

        for(Log log : eventLogs){
            List<String> ppi = new ArrayList<>();
            List<String> ppiNames = new ArrayList<>();

            // PPI1: Process instances
            ppi.add(ppiProcess.processPPI1(log));
            ppiNames.add("Process instances");

            // PPI2: Process steps (Average)
            ppi.add(ppiProcess.processPPI2(log));
            ppiNames.add("Average process events");

            // PPI3: Throughput time (Average)
            // PPI4: Throughput time (Median)
            ppi.add(ppiProcess.processPPI3PPI4(log)[0]);
            ppiNames.add("Average throughput time");
            ppi.add(ppiProcess.processPPI3PPI4(log)[1]);
            ppiNames.add("Median throughput time");

            // PPI5: Time period (Total)
            ppi.add(ppiProcess.processPPI5(log));
            ppiNames.add("Time span");

            result.addProcessPPI(log.getProcessName(), ppi);
            result.addProcessPPIName(log.getProcessName(), ppiNames);
        }
    }

    /**
     * Calculates PPIs for all ISEs of the wait class and for the wait class in general.
     * @param iseCollective
     */
    public void computeWaitPPI(ISECollective iseCollective) {
        PPIWait ppiWait = new PPIWait();

        // ISE PPI
        for (ISESingle iseSingle : iseCollective.getSingleIseList()) {
            // Wait-PPI1: Change in total throughput time
            iseSingle.addPPI(ppiWait.waitPPI1(iseSingle));
            iseSingle.addPPIName("Deviation of duration of trigger task");

            // Wait-PPI2: Deviation of duration of trigger task
            iseSingle.addPPI(ppiWait.waitPPI2(iseSingle));
            iseSingle.addPPIName("Sum of deviation of duration of handling task");

            // Wait-PPI3: Deviation of duration of handling tasks
            iseSingle.addPPI(ppiWait.waitPPI3(iseSingle));
            iseSingle.addPPIName("Average deviation of throughput time");
        }

        this.computeClassPPI(iseCollective, ppiWait);
    }

    public void computeCancelPPI(ISECollective iseCollective) {
        PPICancel ppiCancel = new PPICancel();

        for(ISESingle iseSingle : iseCollective.getSingleIseList()) {
            iseSingle.addPPI(ppiCancel.cancelPPI1(iseSingle));
            iseSingle.addPPIName("Duration of canceling task");

            iseSingle.addPPI(ppiCancel.cancelPPI2(iseSingle));
            iseSingle.addPPIName("Duration of propagation of cancellation of the task");

            iseSingle.addPPI(ppiCancel.cancelPPI3(iseSingle));
            iseSingle.addPPIName("Average deviation of throughput time");
        }

        this.computeClassPPI(iseCollective, ppiCancel);
    }


    public void computeRedoPPI(ISECollective iseCollective) {
        PPIRedo ppiRedo = new PPIRedo();

        for(ISESingle iseSingle : iseCollective.getSingleIseList()) {
            String[] ppi1ppi2 = ppiRedo.redoPPI1PPI2(iseSingle);
            iseSingle.addPPI(ppi1ppi2[0]);
            iseSingle.addPPIName("Iterations");
            iseSingle.addPPI(ppi1ppi2[1]);
            iseSingle.addPPIName("Duration of additional iterations");

            iseSingle.addPPI(ppiRedo.redoPPI3(iseSingle));
            iseSingle.addPPIName("Average deviation of throughput time");
        }

        this.computeClassPPI(iseCollective, ppiRedo);
    }

    public void computeChangePPI(ISECollective iseCollective) {
        PPIChange ppiChange = new PPIChange();

        for(ISESingle iseSingle : iseCollective.getSingleIseList()) {
            iseSingle.addPPI(ppiChange.changePPI1(iseSingle));
            iseSingle.addPPIName("Duration of resource being broken");

            iseSingle.addPPI(ppiChange.changePPI2(iseSingle));
            iseSingle.addPPIName("Duration of trigger task until reassign");

            iseSingle.addPPI(ppiChange.changePPI3(iseSingle));
            iseSingle.addPPIName("Average deviation of throughput time");
        }

        this.computeClassPPI(iseCollective, ppiChange);
    }

    public void computeReworkPPI(ISECollective iseCollective){
        PPIRework ppiRework = new PPIRework();

        for(ISESingle iseSingle : iseCollective.getSingleIseList()) {
            iseSingle.addPPI(ppiRework.reworkPPI1(iseSingle));
            iseSingle.addPPIName("Average number of newly created handling tasks");

            iseSingle.addPPI(ppiRework.reworkPPI2(iseSingle));
            iseSingle.addPPIName("Duration between occurrence and discovery of broken resource");

            iseSingle.addPPI(ppiRework.reworkPPI3(iseSingle));
            iseSingle.addPPIName("Average deviation of throughput time");
        }

        this.computeClassPPI(iseCollective, ppiRework);
    }

    private void computeClassPPI(ISECollective iseCollective, PPICalculation ppiCalculation){
        // General PPI
        // Class: ISE found
        iseCollective.addPPI(ppiCalculation.classPPI1(iseCollective));
        iseCollective.addPPIName("ISE found");

        // Class-PPI2: Processes involved
        // Class-PPI3: Process instances involved
        String[] ppi2Ppi3Results = ppiCalculation.classPPI2PPI3(iseCollective);
        iseCollective.addPPI(ppi2Ppi3Results[0]);
        iseCollective.addPPIName("Processes involved");
        iseCollective.addPPI(ppi2Ppi3Results[1]);
        iseCollective.addPPIName("Process instances involved");

        // Class-PPI4: Sum of deviation of total throughput time
        iseCollective.addPPI(ppiCalculation.classPPI4(iseCollective));
        iseCollective.addPPIName("Sum of deviation of throughput time");
    }
}
