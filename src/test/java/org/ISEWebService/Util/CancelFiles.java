package org.ISEWebService.Util;

import org.deckfour.xes.model.XLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CancelFiles extends XesFile{

    @Override
    public List<ByteArrayOutputStream> create() throws IOException, ParseException {
        // Create Process Templates
        List<ProcessTemplate> processTemplates = new ArrayList<>();
        processTemplates.add(createPatrolPolice());
        processTemplates.add(createDetective());
        processTemplates.add(createHelicopter());

        // Creates Exceptions
        ExceptionCollection exceptionCollection = this.createExceptions();

        // Create XLogs with Exceptions
        List<XLog> xLogList = this.createXLogListWithExceptions(processTemplates, exceptionCollection);

        // Parse XLogList to Files
        // this.serializeXLogToFile(xLogList, "cancel/cancelISE");

        // Parse XLogList to OutputSteam
        return this.serializeXLogToOutputStream(xLogList, "waitISE");
    }

    private ProcessTemplate createPatrolPolice() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Drive into search area", "Search for missing person", "Drive back to station"};
        // Amount of Traces of the Log
        int amountTraces = 7;
        // Duration of a Task
        long durationTaskInMinute = 30;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 5;
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 60 * 24 * 15; // Every 15 d
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2018-03-15T11:00:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "missingPerson";
        // Different merge attribute values
        String[] mergeAttributeValues = {"Mike", "Hannah", "Patrick", "John", "Jimmy", "Sarah", "Sandra"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    private ProcessTemplate createDetective() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Search for missing person", "Drive back to station"};
        // Amount of Traces of the Log
        int amountTraces = 7;
        // Duration of a Task
        long durationTaskInMinute = 30;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 5;
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 60 * 24 * 15; // Every 15 d
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2018-03-15T11:35:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "missingPerson";
        // Different merge attribute values
        String[] mergeAttributeValues = {"Mike", "Hannah", "Patrick", "John", "Jimmy", "Sarah", "Sandra"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    private ProcessTemplate createHelicopter() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Fly into search area", "Search for missing person", "Fly back to station"};
        // Amount of Traces of the Log
        int amountTraces = 7;
        // Duration of a Task
        long durationTaskInMinute = 30;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 5;
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 60 * 24 * 15; // Every 15 d
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2018-03-15T11:00:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "missingPerson";
        // Different merge attribute values
        String[] mergeAttributeValues = {"Mike", "Hannah", "Patrick", "John", "Jimmy", "Sarah", "Sandra"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    private ExceptionCollection createExceptions(){
        // Cancel-Exceptions
        List<ExceptionDefinition> cancelExceptions = new ArrayList<>();

        // Cancel-Exception 1
        cancelExceptions.add(new CancelException(new int[]{0,0,1}, new int[][]{{1,0,0}, {2,0,1}}, 0.9));

        // Cancel-Exception 2
        cancelExceptions.add(new CancelException(new int[]{1,1,0}, new int[][]{{0,1,1}, {2,1,1}}, 0.8));

        // Cancel-Exception 3
        cancelExceptions.add(new CancelException(new int[]{1,2,0}, new int[][]{{0,2,1}, {2,2,1}}, 0.85));

        // Cancel-Exception 4
        cancelExceptions.add(new CancelException(new int[]{1,3,0}, new int[][]{{0,3,1}, {2,3,1}}, 0.9));

        // Cancel-Exception 5
        cancelExceptions.add(new CancelException(new int[]{2,4,1}, new int[][]{{1,4,0}, {0,4,1}}, 0.95));

        // Cancel-Exception 6
        cancelExceptions.add(new CancelException(new int[]{1,5,0}, new int[][]{{2,5,1}, {0,5,1}}, 0.99));

        // Cancel-Exception 7
        cancelExceptions.add(new CancelException(new int[]{1,6,0}, new int[][]{{0,6,1}, {2,6,1}}, 0.75));

        return new ExceptionCollection(ExceptionType.CANCEL_EXCEPTION, cancelExceptions);
    }
}
