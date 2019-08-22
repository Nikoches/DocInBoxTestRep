package DocInBoxTest;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class DocInBoxDateTest {
    protected static final String FILE_PATH = "src/test/resources/properties.json";
    protected final static String TEST_URL = "https://test.dxbx.ru/api/json/v2/importinvoice";
    private final static String CHK_URL = "https://test.dxbx.ru/api/json/v2/checkconnection";
    private final static String RESPONSE_FUTURE = "Invoice № 178939719898 cannot be imported. Invalid Document Date: Thu Nov 01 09:01:02 MSK 2029. Please, contact support. Tel.: +7 800 555-96-79; e-mail: support@docsinbox.ru";
    private final static String RESPONSE_PAST = "Invoice № 178939719898 cannot be imported. Invalid Document Date: Wed Oct 29 09:30:45 MSK 1029. Please, contact support. Tel.: +7 800 555-96-79; e-mail: support@docsinbox.ru";

    /**
     * Method have purpose to get description of HTTP response.
     * Returns the Description String.
     */
    public static String getDescResponse(JSONObject responseJson, String value) {
        JSONObject payloadResponse = (JSONObject) responseJson.get("payload");
        JSONArray documentsResponse = (JSONArray) payloadResponse.get("documents");
        JSONObject jsonObjectResponse = (JSONObject) documentsResponse.get(0);
        return jsonObjectResponse.get(value).toString();
    }

    /**
     * Data provider provides depending on the calling method set of Data.
     */
    @DataProvider
    public Object[][] TestType(Method method) {
        Object[][] returned = null;
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(fileReader);
            JSONObject payload = (JSONObject) jsonObject.get("payload");
            JSONArray documents = (JSONArray) payload.get("documents");
            JSONObject firstDocJson = (JSONObject) documents.get(0);
            if (method.getName().equals("checkFutureDateRequest")) {
                firstDocJson.replace("date", "01.11.2029 09:01:02");
                returned = new Object[][]{{jsonObject, RESPONSE_FUTURE}};
            } else if (method.getName().equals("checkPastDateRequest")) {
                firstDocJson.replace("date", "04.11.1029 09:01:02");
                returned = new Object[][]{{jsonObject, RESPONSE_PAST}};
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return returned;
    }

    /**
     * Simple check connection.
     */
    @Test
    @BeforeTest
    public void checkConnection() {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(CHK_URL);
            /*
            Create JSON to send request
             */
            HttpEntity stringEntity = new StringEntity(("{  \"secretWord\": \"f1988ed5-48ab-4147-b9c6-5f6ed5cb16c6\",  \"inn\": \"123456789012\"}"), ContentType.APPLICATION_JSON);
            httpPost.setEntity(stringEntity);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            /*
            Print & Assert response
             */
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * First test run with future date values.
     */
    @Test(dataProvider = "TestType")
    public void checkFutureDateRequest(JSONObject mainjson, String answer) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            /*
                Sending POST request.
             */
            HttpPost httpPost = new HttpPost(TEST_URL);
            httpPost.setEntity(new StringEntity(
                    mainjson.toString(),
                    ContentType.APPLICATION_JSON));
            CloseableHttpResponse response = httpclient.execute(httpPost);
            /*
                Print & Assert response.
             */
            JSONObject responseJson = (JSONObject) new JSONParser().parse(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
            Assert.assertEquals(getDescResponse(responseJson, "description"), answer);
            Reporter.log(" Test checkFutureDateRequest passed.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Second test run with past date values.
     */
    @Test(dataProvider = "TestType")
    public void checkPastDateRequest(JSONObject mainjson, String answer) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            /*
            Sending POST request
             */
            HttpPost httpPost = new HttpPost(TEST_URL);
            httpPost.setEntity(new StringEntity(
                    mainjson.toString(),
                    ContentType.APPLICATION_JSON));
            CloseableHttpResponse response = httpclient.execute(httpPost);
            /*
            Print & Assert response
             */
            JSONObject responseJson = (JSONObject) new JSONParser().parse(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
            Assert.assertEquals(getDescResponse(responseJson, "description"), answer);
            Reporter.log(" Test checkPastDateRequest passed.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
