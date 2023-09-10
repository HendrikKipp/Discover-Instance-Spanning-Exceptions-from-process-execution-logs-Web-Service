package org.ISEWebService;

import org.ISEWebService.Model.DTO.ISECollective;
import org.ISEWebService.Model.EventLog.Log;
import org.ISEWebService.Model.EventLog.Trace;
import org.ISEWebService.Service.ISEService;
import org.ISEWebService.Service.XESParserService;
import org.ISEWebService.Util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@SpringBootTest
class IseWebServiceApplicationTests {

	@Autowired
	XESParserService xesParserService;

	@Autowired
	ISEService iseService;

	/**
	 * Tests the ISE of the class wait by creating artificial event logs
	 * @throws IOException
	 * @throws ParseException
	 */
	@Test
	public void testISEWait() throws IOException, ParseException {
		// Simulated User Input
		String mergeAttribute = "harbour";
		double waitThreshold = 1;

		// Creates test files with wait exception
		WaitFiles waitFiles = new WaitFiles();
		List<ByteArrayOutputStream> byteArrayOutputStreamList = waitFiles.create();

		// Parses output stream to multipart file
		MultipartFile[] multipartFiles = new MockMultipartFile[byteArrayOutputStreamList.size()];
		int i=0;
		for(ByteArrayOutputStream outputStream : byteArrayOutputStreamList){
			byte[] content = outputStream.toByteArray();
			MultipartFile multipartFile = new MockMultipartFile("testISEWait_" + i, content);
			multipartFiles[i++] = multipartFile;
		}

		// Parse XES-File
		List<Log> parsedEventLogs = xesParserService.parseXESFiles(multipartFiles);

		// Merge Traces
		List<Trace> mergedTraces = iseService.mergeTraces(parsedEventLogs, mergeAttribute);

		// Apply ISE Wait Algorithm
		ISECollective iseCollective = iseService.discoverWait(parsedEventLogs, mergedTraces, waitThreshold);

		// Correct amount
		assertEquals(3, iseCollective.getSingleIseList().size());

		// ISE 1
		assertEquals("instance_86", iseCollective.getSingleIseList().get(0).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(5, iseCollective.getSingleIseList().get(0).getHandling().size());

		// ISE 2
		assertEquals("instance_110", iseCollective.getSingleIseList().get(1).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(3, iseCollective.getSingleIseList().get(1).getHandling().size());

		// ISE 3
		assertEquals("instance_117", iseCollective.getSingleIseList().get(2).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(2, iseCollective.getSingleIseList().get(2).getHandling().size());
	}

	@Test
	public void testISECancel() throws IOException, ParseException {
		// Simulated User Input
		String mergeAttribute = "missingPerson";

		// Creates test files with wait exception
		CancelFiles cancelFiles = new CancelFiles();
		List<ByteArrayOutputStream> byteArrayOutputStreamList = cancelFiles.create();

		// Parses output stream to multipart file
		MultipartFile[] multipartFiles = new MockMultipartFile[byteArrayOutputStreamList.size()];
		int i=0;
		for(ByteArrayOutputStream outputStream : byteArrayOutputStreamList){
			byte[] content = outputStream.toByteArray();
			MultipartFile multipartFile = new MockMultipartFile("testISECancel_" + i, content);
			multipartFiles[i++] = multipartFile;
		}

		// Parse XES-File
		List<Log> parsedEventLogs = xesParserService.parseXESFiles(multipartFiles);

		// Merge Traces
		List<Trace> mergedTraces = iseService.mergeTraces(parsedEventLogs, mergeAttribute);

		// Apply ISE Wait Algorithm
		ISECollective iseCollective = iseService.discoverCancel(mergedTraces);
		System.out.println(iseCollective.getSingleIseList().size());

		// Correct amount
		assertEquals(7, iseCollective.getSingleIseList().size());

		// ISE 1
		assertEquals("instance_0", iseCollective.getSingleIseList().get(0).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(2, iseCollective.getSingleIseList().get(0).getHandling().size());

		// ISE 2
		assertEquals("instance_1", iseCollective.getSingleIseList().get(1).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(2, iseCollective.getSingleIseList().get(1).getHandling().size());

		// ISE 3
		assertEquals("instance_2", iseCollective.getSingleIseList().get(2).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(2, iseCollective.getSingleIseList().get(2).getHandling().size());

		// ISE 4
		assertEquals("instance_3", iseCollective.getSingleIseList().get(3).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(2, iseCollective.getSingleIseList().get(3).getHandling().size());

		// ISE 5
		assertEquals("instance_4", iseCollective.getSingleIseList().get(4).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(2, iseCollective.getSingleIseList().get(4).getHandling().size());

		// ISE 6
		assertEquals("instance_5", iseCollective.getSingleIseList().get(5).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(2, iseCollective.getSingleIseList().get(5).getHandling().size());

		// ISE 7
		assertEquals("instance_6", iseCollective.getSingleIseList().get(6).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(2, iseCollective.getSingleIseList().get(6).getHandling().size());
		 }

	@Test
	public void testISERedo() throws IOException, ParseException {
		// Simulated User Input
		String mergeAttribute = "song";

		// Creates test files with wait exception
		RedoFiles redoFiles = new RedoFiles();
		List<ByteArrayOutputStream> byteArrayOutputStreamList = redoFiles.create();

		// Parses output stream to multipart file
		MultipartFile[] multipartFiles = new MockMultipartFile[byteArrayOutputStreamList.size()];
		int i=0;
		for(ByteArrayOutputStream outputStream : byteArrayOutputStreamList){
			byte[] content = outputStream.toByteArray();
			MultipartFile multipartFile = new MockMultipartFile("testISERedo_" + i, content);
			multipartFiles[i++] = multipartFile;
		}

		// Parse XES-File
		List<Log> parsedEventLogs = xesParserService.parseXESFiles(multipartFiles);

		// Merge Traces
		List<Trace> mergedTraces = iseService.mergeTraces(parsedEventLogs, mergeAttribute);

		// Apply ISE Wait Algorithm
		ISECollective iseCollective = iseService.discoverRedo(mergedTraces);
		System.out.println(iseCollective.getSingleIseList().size());

		// Correct amount
		assertEquals(2, iseCollective.getSingleIseList().size());

		// ISE 1
		assertEquals("instance_0", iseCollective.getSingleIseList().get(0).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(9, iseCollective.getSingleIseList().get(0).getHandling().size());

		// ISE 2
		assertEquals("instance_2", iseCollective.getSingleIseList().get(1).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(5, iseCollective.getSingleIseList().get(1).getHandling().size());
	}

	@Test
	public void testISEChange() throws IOException, ParseException {
		// Simulated User Input
		String mergeAttribute = "originalPrinter";

		// Creates test files with change exception
		ChangeFiles changeFiles = new ChangeFiles();
		List<ByteArrayOutputStream> byteArrayOutputStreamList = changeFiles.create();

		// Parses output stream to multipart file
		MultipartFile[] multipartFiles = new MockMultipartFile[byteArrayOutputStreamList.size()];
		int i=0;
		for(ByteArrayOutputStream outputStream : byteArrayOutputStreamList){
			byte[] content = outputStream.toByteArray();
			MultipartFile multipartFile = new MockMultipartFile("testISEChange_" + i, content);
			multipartFiles[i++] = multipartFile;
		}

		// Parse XES-File
		List<Log> parsedEventLogs = xesParserService.parseXESFiles(multipartFiles);

		// Merge Traces
		List<Trace> mergedTraces = iseService.mergeTraces(parsedEventLogs, mergeAttribute);

		// Apply ISE Wait Algorithm
		ISECollective iseCollective = iseService.discoverChange(mergedTraces);
		System.out.println(iseCollective.getSingleIseList().size());

		// Correct amount
		assertEquals(2, iseCollective.getSingleIseList().size());

		// ISE 1
		assertEquals("instance_48", iseCollective.getSingleIseList().get(0).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(4, iseCollective.getSingleIseList().get(0).getHandling().size());

		// ISE 2
		assertEquals("instance_92", iseCollective.getSingleIseList().get(1).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(3, iseCollective.getSingleIseList().get(1).getHandling().size());
	}

	@Test
	public void testISERework() throws IOException, ParseException {
		// Simulated User Input
		String mergeAttribute = "company";

		// Creates test files with rework exception
		ReworkFiles reworkFiles = new ReworkFiles();
		List<ByteArrayOutputStream> byteArrayOutputStreamList = reworkFiles.create();

		// Parses output stream to multipart file
		MultipartFile[] multipartFiles = new MockMultipartFile[byteArrayOutputStreamList.size()];
		int i=0;
		for(ByteArrayOutputStream outputStream : byteArrayOutputStreamList){
			byte[] content = outputStream.toByteArray();
			MultipartFile multipartFile = new MockMultipartFile("testISERework_" + i, content);
			multipartFiles[i++] = multipartFile;
		}

		// Parse XES-File
		List<Log> parsedEventLogs = xesParserService.parseXESFiles(multipartFiles);

		// Merge Traces
		List<Trace> mergedTraces = iseService.mergeTraces(parsedEventLogs, mergeAttribute);

		// Apply ISE Wait Algorithm
		ISECollective iseCollective = iseService.discoverRework(mergedTraces);
		System.out.println(iseCollective.getSingleIseList().size());

		// Correct amount
		assertEquals(2, iseCollective.getSingleIseList().size());

		// ISE 1
		assertEquals("instance_50", iseCollective.getSingleIseList().get(0).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(24, iseCollective.getSingleIseList().get(0).getHandling().size()); // (1 trigger + 5 handlings) * 2 handling task * 2 events/task = 24

		// ISE 2
		assertEquals("instance_204", iseCollective.getSingleIseList().get(1).getTrigger().getOriginalTrace().getTraceName());
		assertEquals(32, iseCollective.getSingleIseList().get(1).getHandling().size()); // (1 trigger + 7 handlings) * 2 handling task * 2 events/task = 32
	}
}
