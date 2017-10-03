import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.*;

import org.apache.commons.csv.*;


/**
 * A simple example of how to access the Google Analytics API.
 */
public class HelloAnalytics {

    //CSV writing part
    //Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final Object [] FILE_HEADER = {"User Type","Referral Path","Time on Page","Page Views","Exit Rate"};
    private static ArrayList<ArrayList<String>> csvArray = new ArrayList<>(); //This is arraylist object where everything is written
    private static FileWriter fileWriter = null;
    private static CSVPrinter csvPrinter = null;
    private static CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

    // Path to client_secrets.json file downloaded from the Developer's Console.
    // The path is relative to HelloAnalytics.java.
    private static final String CLIENT_SECRET_JSON_RESOURCE = "client_secret.json";

    // Replace with your view ID.
    private static final String VIEW_ID = "58924295";

    // The directory where the user's credentials will be stored.
    private static final File DATA_STORE_DIR = new File(
            System.getProperty("user.home"), ".store/hello_analytics");

    private static final String APPLICATION_NAME = "Hello Analytics Reporting";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static NetHttpTransport httpTransport;
    private static FileDataStoreFactory dataStoreFactory;

    public static void main(String[] args) {
        try {
            AnalyticsReporting service = initializeAnalyticsReporting();

            GetReportsResponse response = getReport(service);
            printResponse(response);
            responseToCSV();
          //  printArrayList();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Initializes an authorized Analytics Reporting service object.
     *
     * @return The analytics reporting service object.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    private static AnalyticsReporting initializeAnalyticsReporting() throws GeneralSecurityException, IOException {

        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(HelloAnalytics.class
                        .getResourceAsStream(CLIENT_SECRET_JSON_RESOURCE)));

        // Set up authorization code flow for all authorization scopes.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(httpTransport, JSON_FACTORY, clientSecrets,
                AnalyticsReportingScopes.all()).setDataStoreFactory(dataStoreFactory)
                .build();

        // Authorize.
        Credential credential = new AuthorizationCodeInstalledApp(flow,
                new LocalServerReceiver()).authorize("user");
        // Construct the Analytics Reporting service object.
        return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Query the Analytics Reporting API V4.
     * Constructs a request for the sessions for the past seven days.
     * Returns the API response.
     *
     * @param service
     * @return GetReportResponse
     * @throws IOException
     */
    private static GetReportsResponse getReport(AnalyticsReporting service) throws IOException {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        dateRange.setStartDate("30DaysAgo");
        dateRange.setEndDate("today");

        // Create the Metrics object.
        Metric timeOnPage = new Metric()
                .setExpression("ga:timeOnPage")
                .setAlias("Time on Page");

        // Create the Metrics object.
        Metric pageviews = new Metric()
                .setExpression("ga:pageviews")
                .setAlias("Page Views");

        // Create the Metrics object.
        Metric exitRate = new Metric()
                .setExpression("ga:exitRate")
                .setAlias("Exit Rate");

        //Create the Dimensions object.
        Dimension hostname = new Dimension()
                .setName("ga:userType");

        //Create the Dimensions object.
        Dimension dimension3 = new Dimension()
                .setName("ga:referralPath");

        //Create the Dimensions object.
        Dimension dimension4 = new Dimension()
                .setName("ga:pagePathLevel2");

        //Make metric filter
        MetricFilter pageFilter = new MetricFilter()
                .setOperator("GREATER_THAN")
                .setComparisonValue("4")
                .setMetricName("ga:pageviews");


        //Make metric filter clauses
        MetricFilterClause metricFilterClause = new MetricFilterClause()
                .setFilters(Arrays.asList(pageFilter));

        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
                .setViewId(VIEW_ID)
                .setDateRanges(Arrays.asList(dateRange))
                .setDimensions(Arrays.asList(hostname, dimension3))
                .setMetrics(Arrays.asList(timeOnPage, pageviews, exitRate))
                .setMetricFilterClauses(Arrays.asList(metricFilterClause));

        ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
        requests.add(request);

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest()
                .setReportRequests(requests);

        // Call the batchGet method.
        GetReportsResponse response = service.reports().batchGet(getReport).execute();

        // Return the response.
        return response;
    }

    /**
     * Parses and prints the Analytics Reporting API V4 response.
     *
     * @param response the Analytics Reporting API V4 response.
     */
    private static void printResponse(GetReportsResponse response) {

        for (Report report: response.getReports()) {
            ColumnHeader header = report.getColumnHeader();
            List<String> dimensionHeaders = header.getDimensions();
            List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
            List<ReportRow> rows = report.getData().getRows();

            if (rows == null) {
                System.out.println("No data found for " + VIEW_ID);
                return;
            }

            for (ReportRow row: rows) {

                ArrayList<String> rowArray = new ArrayList<>();

                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();
                for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
                 //   System.out.println(dimensionHeaders.get(i) + ": " + dimensions.get(i));
                    rowArray.add(dimensions.get(i));
                }

                for (int j = 0; j < metrics.size(); j++) {
                    //System.out.println("Date Range (" + j + "): ");
                    DateRangeValues values = metrics.get(j);
                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                   //     System.out.println(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));

                        rowArray.add(values.getValues().get(k));
                    }
                }
                csvArray.add(rowArray);
            }
        }
    }

    private static void responseToCSV(){

        try{
            fileWriter = new FileWriter("test.csv");
            csvPrinter = new CSVPrinter(fileWriter, csvFormat);

            csvPrinter.printRecord(FILE_HEADER);

            for(ArrayList eachRow:csvArray){
                csvPrinter.printRecord(eachRow);
            }

            System.out.println("File Created!");

        } catch (Exception e){
            System.out.println("Error when creating file");
        } finally {
            try{
                fileWriter.flush();
                fileWriter.close();
                csvPrinter.close();

            } catch (IOException e){
                System.out.println("Couldn't close file");
            }
        }
    }

    private static void printArrayList(){
        for(Object output:csvArray){
            System.out.println(output);
        }
    }
}
