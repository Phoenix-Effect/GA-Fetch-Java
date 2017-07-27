# Google-Analytics-to-CSV
Simple Java client to fetch data from Google Analytics and export to CSV.

## Requires 
* Google Analytics reporting API library v4
    * Can be downloaded from [here](https://developers.google.com/api-client-library/java/apis/analyticsreporting/v4) with documentation to include in your project in Step 2 over [here](https://developers.google.com/analytics/devguides/reporting/core/v4/quickstart/installed-java) for different IDE's. 
* Apache Commons CSV (for exporting to csv)
    * Can be downloaded from [here](https://mvnrepository.com/artifact/org.apache.commons/commons-csv/1.4)

## Getting started
Follow the first step over [here](https://developers.google.com/analytics/devguides/reporting/core/v4/quickstart/installed-java) and download the JSON secret file, rename it to __client_secrets.json__ and put it in the same folder as the program then run the program. If everything goes fine the program will generate a file __test.csv__ in the same folder. 


For changing what sort of data is fetched change the dimensions and metrics. A complete list of both is available [here](https://developers.google.com/analytics/devguides/reporting/core/dimsmets)