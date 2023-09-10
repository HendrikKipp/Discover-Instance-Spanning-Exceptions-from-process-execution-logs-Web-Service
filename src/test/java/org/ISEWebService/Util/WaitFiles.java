package org.ISEWebService.Util;

import org.deckfour.xes.model.XLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WaitFiles extends XesFile{

    @Override
    public List<ByteArrayOutputStream> create() throws IOException, ParseException {
        // Creates Process Templates
        List<ProcessTemplate> processTemplates = new ArrayList<>();
        processTemplates.add(createPassengerShipTransport());
        processTemplates.add(createCargoShipTransport());

        // Creates Exceptions
        ExceptionCollection exceptionCollection = this.createExceptions();

        // Create XLogs with Exceptions
        List<XLog> xLogList = this.createXLogListWithExceptions(processTemplates, exceptionCollection);

        // Parse XLogList to Files
        // this.serializeXLogToFile(xLogList, "wait/waitISE");

        // Parse XLogList to OutputSteam
        return this.serializeXLogToOutputStream(xLogList, "waitISE");
    }

    /**
     * Creates a Process Template for a Passenger Ship Transport
     * @return
     * @throws ParseException
     */
    private ProcessTemplate createPassengerShipTransport() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Transport passengers", "Let passengers off and new ones one"};
        // Amount of Traces of the Log
        int amountTraces = 120;
        // Duration of a Task
        long durationTaskInMinute = 20;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 60; // 3 * 20
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 40; // 2 * 20
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2023-07-15T04:00:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "harbour";
        // Different merge attribute values
        String[] mergeAttributeValues = {"Hamburg", "Rotterdam"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    /**
     * Creates a Process Template for a Cargo Ship Transport
     * @return
     * @throws ParseException
     */
    private ProcessTemplate createCargoShipTransport() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Transport goods", "Prepare goods for discharge", "Discharge and reload ship"};
        // Amount of Traces of the Log
        int amountTraces = 90;
        // Duration of a Task
        long durationTaskInMinute = 20;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 60; // 3 * 20
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 40; // 2 * 20
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2023-07-15T02:20:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "harbour";
        // Different merge attribute values
        String[] mergeAttributeValues = {"Hamburg", "Rotterdam"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    /**
     * Creates the Wait-Exceptions
     * @return
     */

    private ExceptionCollection createExceptions(){
        // Wait-Exceptions
        List<ExceptionDefinition> waitExceptions = new ArrayList<>();

        // Wait-Exception 1
        Map<String, Double> nextTraceDelayPerLog1 = new HashMap<>();
        nextTraceDelayPerLog1.put("0, 88, 1", 3.5);
        waitExceptions.add(new WaitException(4, new double[]{3, 2, 3.5, 2.5, 1.5}, new int[]{0, 86, 1}, new int[][]{{0, 87, 1},{0, 88, 1},{1, 87, 2},{1, 88, 2},{1, 89, 2}}, nextTraceDelayPerLog1));

        // Wait-Exception 2
        Map<String, Double> nextTraceDelayPerLog2 = new HashMap<>();
        nextTraceDelayPerLog2.put("0, 113, 1", 3.0);
        waitExceptions.add(new WaitException(3.5, new double[]{2.5, 2, 2}, new int[]{0, 110, 1}, new int[][]{{0, 111, 1},{0, 112, 1},{0, 113, 1}}, nextTraceDelayPerLog2));

        // Wait-Exception 3
        Map<String, Double> nextTraceDelayPerLog3 = new HashMap<>();
        nextTraceDelayPerLog3.put("0, 119, 1", 3.0);
        waitExceptions.add(new WaitException(3.5, new double[]{2.5, 2, 2}, new int[]{0, 117, 1}, new int[][]{{0, 118, 1},{0, 119, 1}}, nextTraceDelayPerLog3));


        return new ExceptionCollection(ExceptionType.WAIT_EXCEPTION, waitExceptions);
    }
}
