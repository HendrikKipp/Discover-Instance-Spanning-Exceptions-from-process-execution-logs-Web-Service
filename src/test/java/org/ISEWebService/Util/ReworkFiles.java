package org.ISEWebService.Util;

import org.deckfour.xes.model.XLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReworkFiles extends XesFile{
    @Override
    public List<ByteArrayOutputStream> create() throws IOException, ParseException {
        // Create Process Templates
        List<ProcessTemplate> processTemplates = new ArrayList<>();
        processTemplates.add(createPaySalary());
        processTemplates.add(createPayInvoice());

        // Creates Exceptions
        ExceptionCollection exceptionCollection = this.createExceptions();

        // Create XLogs with Exceptions
        List<XLog> xLogList = this.createXLogListWithExceptions(processTemplates, exceptionCollection);

        // Parse XLogList to Files
        // this.serializeXLogToFile(xLogList, "rework/reworkISE");

        // Parse XLogList to OutputSteam
        return this.serializeXLogToOutputStream(xLogList, "reworkISE");
    }

    private ProcessTemplate createPaySalary() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Check hours worked", "Pay salary", "Inform employee"};
        // Amount of Traces of the Log
        int amountTraces = 205;
        // Duration of a Task
        long durationTaskInMinute = 10;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 5;
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 60;
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2022-03-15T11:00:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "company";
        // Different merge attribute values
        String[] mergeAttributeValues = {"Alpha", "Beta"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    private ProcessTemplate createPayInvoice() throws ParseException {
        // The events of the process model
        String[] traceTemplate = {"Check invoices", "Pay invoices"};
        // Amount of Traces of the Log
        int amountTraces = 200;
        // Duration of a Task
        long durationTaskInMinute = 10;
        // Duration between the Tasks of a Trace
        long durationBetweenTasksInMinute = 5;
        // Duration between the start times of the Traces of the Log
        long durationBetweenTraceStarts = 60;
        // First timestamp of the Log
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date firstTimestamp = format.parse("2022-03-15T11:00:00.000+02:00");
        // Merge attribute
        String mergeAttribute = "company";
        // Different merge attribute values
        String[] mergeAttributeValues = {"Alpha", "Beta"};
        //String[] mergeAttributeValues = {"Beta"};

        return new ProcessTemplate(traceTemplate, amountTraces, durationTaskInMinute, durationBetweenTasksInMinute, durationBetweenTraceStarts, firstTimestamp, mergeAttribute, mergeAttributeValues);
    }

    private ExceptionCollection createExceptions(){
        // Rework-Exceptions
        List<ExceptionDefinition> reworkException = new ArrayList<>();

        // Rework-Exception 1
        reworkException.add(new ReworkException(new int[]{1, 50, 1}, new int[][]{{0, 49, 1}, {0, 48, 1}, {0, 47, 1}, {1, 46, 1}, {1, 45, 1}}, new String[]{"Calculate remaining amount", "Pay remaining amount"}));

        // Rework-Exception 2
        reworkException.add(new ReworkException(new int[]{0, 204, 1}, new int[][]{{0, 203, 1}, {0, 202, 1}, {0, 201, 1}, {0, 200, 1}, {0, 199, 1}, {1, 199, 1}, {1, 198, 1}}, new String[]{"Calculate remaining amount", "Pay remaining amount"}));

        return new ExceptionCollection(ExceptionType.REWORK_EXCEPTION, reworkException);
    }
}
