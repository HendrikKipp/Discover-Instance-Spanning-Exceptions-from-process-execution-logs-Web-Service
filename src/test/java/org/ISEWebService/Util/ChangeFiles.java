package org.ISEWebService.Util;

import org.deckfour.xes.model.XLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChangeFiles extends XesFile{

    @Override
    public List<ByteArrayOutputStream> create() throws IOException, ParseException {
        // Create Process Templates
        List<ProcessTemplate> processTemplates = new ArrayList<>();
        processTemplates.add(createPrintDocument());
        processTemplates.add(createScanDocument());

        // Creates Exceptions
        ExceptionCollection exceptionCollection = this.createExceptions();

        // Create XLogs with Exceptions
        List<XLog> xLogList = this.createXLogListWithExceptions(processTemplates, exceptionCollection);

        // Parse XLogList to Files
        // this.serializeXLogToFile(xLogList, "change/changeISE");

        // Parse XLogList to OutputSteam
        return this.serializeXLogToOutputStream(xLogList, "changeISE");
    }

    private ProcessTemplate createPrintDocument() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Prepare document for printing", "Print document"};
        // Amount of Traces of the Log
        int amountTraces = 120;
        // Duration of a Task
        long durationTaskInMinute = 20;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 60; // 3 * 20
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 20;
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2023-02-10T10:00:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "originalPrinter";
        // Different merge attribute values
        String[] mergeAttributeValues = {"printer1", "printer2", "printer3"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    private ProcessTemplate createScanDocument() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Prepare document for scanning", "Scan document"};
        // Amount of Traces of the Log
        int amountTraces = 50;
        // Duration of a Task
        long durationTaskInMinute = 20;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 60; // 3 * 20
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 20;
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2023-02-10T10:10:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "originalPrinter";
        // Different merge attribute values
        String[] mergeAttributeValues = {"printer1", "printer2"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    private ExceptionCollection createExceptions(){
        // Change-Exceptions
        List<ExceptionDefinition> changeExceptions = new ArrayList<>();

        // Change-Exception 1
        Map<String, Double> nextTraceDelayPerLog1 = new HashMap<>();
        nextTraceDelayPerLog1.put("0, 50, 1", 4.0);
        changeExceptions.add(new ChangeException(new int[]{0, 48, 1}, new int[][]{{0, 49, 1}, {0, 50, 1}, {1, 48, 1}, {1, 49, 1}}, nextTraceDelayPerLog1, "printer5"));

        // Change-Exception 2
        Map<String, Double> nextTraceDelayPerLog2 = new HashMap<>();
        nextTraceDelayPerLog2.put("0, 95, 1", 4.0);
        changeExceptions.add(new ChangeException(new int[]{0, 92, 1}, new int[][]{{0, 93, 1}, {0, 94, 1}, {0, 95, 1}}, nextTraceDelayPerLog2, "printer0"));

        return new ExceptionCollection(ExceptionType.CHANGE_EXCEPTION, changeExceptions);
    }
}
