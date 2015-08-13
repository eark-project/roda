package pt.gov.dgarq.roda.services.client;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.stubs.LogMonitor;
import pt.gov.dgarq.roda.core.stubs.Logger;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * Test class for {@link Logger} and {@link LogMonitor} service.
 * 
 * @author Rui Castro
 */
public class LogMonitorTest {

	public static void main(String[] args) {

		RODAClient rodaClient = null;

		try {

			if (args.length == 4) {

				// http://localhost:8180/
				String hostUrl = args[0];
				String casURL = args[1];
				String coreURL = args[2];
				String serviceURL = args[3];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				rodaClient = new RODAClient(new URL(hostUrl),casUtility);

			} else if (args.length >= 6) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];
				String casURL = args[3];
				String coreURL = args[4];
				String serviceURL = args[5];
				CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));
				rodaClient = new RODAClient(new URL(hostUrl), username,
						password,casUtility);
			} else {
				System.err.println(LogMonitorTest.class.getSimpleName()
						+ " protocol://hostname:port/ [username password] casURL coreURL serviceURL");
				System.exit(1);
			}

			LogMonitor logMonitorService = rodaClient.getLogMonitorService();

			System.out.println("\n******************************************");
			System.out.println("* Log entries with a ContentAdapter");
			System.out.println("******************************************");

			int logEntriesCount = logMonitorService.getLogEntriesCount(null);
			System.out.println("ALL Log Entries Count: " + logEntriesCount);

//			Filter filter = new Filter(
//					new FilterParameter[] { new SimpleFilterParameter(
//							"username", "guest") });
//
//			logEntriesCount = logMonitorService.getLogEntriesCount(filter);
//			System.out.println("GUEST Log Entries Count: " + logEntriesCount);
//
//			// FIXME
//			// filter
//			// .setParameters(new FilterParameter[] { new
//			// OneOfManyFilterParameter(
//			// "username", new String[] { "guest", "demo-admin" }) });
//
//			logEntriesCount = logMonitorService.getLogEntriesCount(filter);
//			System.out.println("GUEST or demo-admin Log Entries Count: "
//					+ logEntriesCount);
//
//			System.out.println("\n******************************************");
//			System.out
//					.println("* Maximum of 30 Log Entries for guest or demo-admin");
//			System.out.println("******************************************");
//
//			ContentAdapter cAdapter = new ContentAdapter(filter, null, null);
//
//			// maximum of 10 results
//			cAdapter.setSublist(new Sublist(0, 30));
//
//			LogEntry[] logEntries = logMonitorService.getLogEntries(cAdapter);
//			List<LogEntry> asList = Arrays.asList(logEntries);
//			for (Iterator<LogEntry> iterator = asList.iterator(); iterator
//					.hasNext();) {
//				LogEntry logEntry = iterator.next();
//				System.out.println(logEntry);
//			}
//
//			System.out
//					.println("\n****************************************************************");
//			System.out
//					.println("* Maximum of 30 Log Entries for guest or demo-admin ordered by ascending datetime");
//			System.out
//					.println("****************************************************************");
//
//			Sorter sorter = new Sorter(new SortParameter[] { new SortParameter(
//					"datetime", false) });
//			cAdapter.setSorter(sorter);
//
//			logEntries = logMonitorService.getLogEntries(cAdapter);
//
//			asList = Arrays.asList(logEntries);
//			for (Iterator<LogEntry> iterator = asList.iterator(); iterator
//					.hasNext();) {
//				LogEntry logEntry = (LogEntry) iterator.next();
//				System.out.println(logEntry);
//			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
