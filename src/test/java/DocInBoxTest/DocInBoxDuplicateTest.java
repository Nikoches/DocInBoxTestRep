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

public class DocInBoxDuplicateTest {
    private static final String DUPLICATE_DOCUMENT = "duplicate_document";

    /**
     * Data provider provides depending on the calling method set of Data.
     */
    @DataProvider
    public Object[][] TestType2(Method method) {
        Object[][] returned = null;
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(fileReader);
            returned = new Object[][]{{jsonObject, DUPLICATE_DOCUMENT}};
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return returned;
    }

    /**
     * First test run with duplicate data values.
     */
    @Test(dataProvider = "TestType2")
    public void checkDuplicateDocumentRequest(JSONObject mainjson, String answer) {
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
            Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
            Assert.assertEquals(getDescResponse(responseJson, "errorType"), answer);
            Reporter.log(" Test checkDuplicateDocumentRequest passed.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
