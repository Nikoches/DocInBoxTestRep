package DocInBoxTest;

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
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static DocInBoxTest.DocInBoxDateTest.*;

public class DocInBoxInvalidParametrsTest {
    private static final String INCORRECT_REQ = "incorrect_requisites";
    private static final String INCORRECT_SYSTEM_ID = "incorrect_json";

    /**
     * Data provider provides depending on the calling method set of Data.
     */
    @DataProvider
    public Object[][] TestType3(Method method) {
        Object[][] returned = null;
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(fileReader);
            JSONObject payload = (JSONObject) jsonObject.get("payload");
            JSONArray documents = (JSONArray) payload.get("documents");
            JSONObject firstDocJson = (JSONObject) documents.get(0);
            JSONObject buyerJson = (JSONObject) firstDocJson.get("seller");
            if (method.getName().equals("checkIncorrectBuyerInnRequest")) {
                buyerJson.replace("inn", "aaaaaaaaaaaa");
                returned = new Object[][]{{jsonObject, INCORRECT_REQ}};
            } else if (method.getName().equals("checkIncorrectSystemInfo")) {
                jsonObject.replace("systemInfo", "system=1C7;version=2.12.32.32;configVersion=1С бухгалтерия 10.3;clientVersion=1.2.3;ssl=true");
                returned = new Object[][]{{jsonObject, INCORRECT_SYSTEM_ID}};
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return returned;
    }

    /**
     * First test run with invalid  buyer requsites data values.
     */
    @Test(dataProvider = "TestType3")
    public void checkIncorrectBuyerInnRequest(JSONObject mainjson, String answer) {
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
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 500);
            Assert.assertEquals(responseJson.get("errorType").toString(), answer);
            Reporter.log(" Test checkIncorrectBuyerInnRequest passed.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * First test run with invalid  system id data values.
     */
    @Test(dataProvider = "TestType3")
    public void checkIncorrectSystemInfo(JSONObject mainjson, String answer) {
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
            System.out.println(responseJson.toString());
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 500);
            Assert.assertEquals(responseJson.get("errorType").toString(), answer);
            Reporter.log(" Test checkIncorrectSystemInfo passed.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
