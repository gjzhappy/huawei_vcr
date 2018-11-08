package com.duplicall.screenAnalyse.commons.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.ais.sdk.util.HttpClientUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * 使用Token认证方式访问服务
 *
 * @author Sean
 */
public class VideoOcrUtil {
    //连接目标url超时限制参数
    public static int connectionTimeout = 5000;
    //连接池获取可用连接超时限制参数
    public static int connectionRequestTimeout = 5000;
    //获取服务器响应数据超时限制参数
    public static int socketTimeout = 5000;

    private static Logger logger = LoggerFactory.getLogger(VideoOcrUtil.class);

    /**
     * 构造使用Token方式访问服务的请求Token对象
     *
     * @param username    用户名
     * @param passwd      密码
     * @param domainName  域名
     * @param projectName 项目名称
     * @return 构造访问的JSON对象
     */
    private static String requestBody(String username, String passwd, String domainName, String projectName) {
        JSONObject auth = new JSONObject();

        JSONObject identity = new JSONObject();

        JSONArray methods = new JSONArray();
        methods.add("password");
        identity.put("methods", methods);

        JSONObject password = new JSONObject();

        JSONObject user = new JSONObject();
        user.put("name", username);
        user.put("password", passwd);

        JSONObject domain = new JSONObject();
        domain.put("name", domainName);
        user.put("domain", domain);

        password.put("user", user);

        identity.put("password", password);

        JSONObject scope = new JSONObject();

        JSONObject scopeProject = new JSONObject();
        scopeProject.put("name", projectName);

        scope.put("project", scopeProject);

        auth.put("identity", identity);
        auth.put("scope", scope);

        JSONObject params = new JSONObject();
        params.put("auth", auth);
        return params.toJSONString();
    }

    /**
     * 获取Token参数， 注意，此函数的目的，主要为了从HTTP请求返回体中的Header中提取出Token
     * 参数名为: X-Subject-Token
     *
     * @param username    用户名
     * @param password    密码
     * @param projectName 区域名，可以参考http://developer.huaweicloud.com/dev/endpoint
     * @return 包含Token串的返回体，
     * @throws URISyntaxException
     * @throws UnsupportedOperationException
     * @throws IOException
     */
    public static String getToken(String username, String password, String projectName)
            throws URISyntaxException, UnsupportedOperationException, IOException {
        String requestBody = requestBody(username, password, username, projectName);
        String url = "https://iam.cn-north-1.myhuaweicloud.com/v3/auth/tokens";

        Header[] headers = new Header[]{new BasicHeader("Content-Type", ContentType.APPLICATION_JSON.toString())};
        StringEntity stringEntity = new StringEntity(requestBody,
                "utf-8");

        HttpResponse response = HttpClientUtils.post(url, headers, stringEntity, connectionTimeout, connectionRequestTimeout, socketTimeout);
        Header[] xst = response.getHeaders("X-Subject-Token");
        return xst[0].getValue();

    }

    public static String postVideoOcrBody(String taskName, String description, String bucketName, String... filePath) {
        JSONObject info = new JSONObject();
        info.put("taskName", taskName);
        info.put("description", description);
        info.put("serviceVersion", "1.0");

        JSONObject input = new JSONObject();

        input.put("type", "obs");
        JSONArray data = new JSONArray();
        for (String s : filePath) {
            JSONObject dataInfo = new JSONObject();
            dataInfo.put("bucket", bucketName);
            dataInfo.put("path", s);
            data.add(dataInfo);
        }
        input.put("data", data);
        info.put("input", input);

        JSONObject output = new JSONObject();
        JSONObject obs = new JSONObject();
        obs.put("bucket", bucketName);
        obs.put("path", "output/");
        output.put("obs", obs);
        info.put("output", output);
        JSONObject serviceConfig = new JSONObject();
        JSONObject common = new JSONObject();
        common.put("area", "0,0,0.5,0.5;");
//        common.put("threshold", "0.5");
        serviceConfig.put("common", common);
        info.put("serviceConfig", serviceConfig);
        return info.toJSONString();


    }

    /*创建视频OCR作业*/
    public static String requestVideoOcr(String token, String projectId, String taskName, String bucketName, String... filePath) {
        String url = "https://iva.cn-north-1.myhuaweicloud.com/v1/" + projectId + "/services/video-ocr/tasks";
        Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token), new BasicHeader("Content-Type", "application/json")};
        String requestBody = postVideoOcrBody(taskName, "video-ocr-demo", bucketName, filePath);
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");
        try {
            HttpResponse response = HttpClientUtils.post(url, headers, stringEntity, connectionTimeout, connectionRequestTimeout, socketTimeout);
            Charset charset = Charset.forName("utf-8");
            String content = IOUtils.toString(response.getEntity().getContent(), charset);
            logger.info(content);
            JSONArray jsonArray = JSONArray.parseArray(content);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if (jsonObject.containsKey("id")) {
                return jsonObject.getString("id");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /*删除视频OCR作业*/
    public static void deleteVideoOcr(String token, String projectId, String taskId) {
        String url = "https://iva.cn-north-1.myhuaweicloud.com/v1/" + projectId + "/services/video-ocr/tasks/" + taskId;
        Header header = new BasicHeader("X-Auth-Token", token);
        Header header2 = new BasicHeader("Content-Type", "application/json");
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpDelete httpDelete = new HttpDelete(url);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(connectionRequestTimeout)
                    .setConnectTimeout(connectionTimeout)
                    .build();
            httpDelete.setConfig(requestConfig);
            httpDelete.addHeader(header);
            httpDelete.addHeader(header2);
            CloseableHttpResponse response = httpClient.execute(httpDelete);
            logger.info(response.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /*查看单个视频OCR作业进度, 作业完成返回true*/
    public static Boolean getVideoOcrStatus(String token, String projectId, String taskId) {
        Boolean result = false;
        String url = "https://iva.cn-north-1.myhuaweicloud.com/v1/" + projectId + "/services/video-ocr/tasks/" + taskId;
        Header header = new BasicHeader("X-Auth-Token", token);
        Header header2 = new BasicHeader("Content-Type", "application/json");
        ReadableByteChannel rchannel = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(connectionRequestTimeout)
                    .setConnectTimeout(connectionTimeout)
                    .build();
            httpGet.setConfig(requestConfig);
            httpGet.addHeader(header);
            httpGet.addHeader(header2);
            CloseableHttpResponse response = httpClient.execute(httpGet);
//            logger.info(response.getEntity().toString());
            rchannel = Channels.newChannel(response.getEntity().getContent());
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            while (rchannel.read(buffer) != -1) {
                buffer.flip();
                Charset charset = Charset.forName("UTF-8");
                CharsetDecoder decoder = charset.newDecoder();
                CharBuffer charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
                buffer.clear();
                if (charBuffer.toString().indexOf("\"state\":\"SUCCEEDED\"") > -1) {
                    result = true;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                rchannel.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return result;
    }


    /**
     * 调用主入口函数
     */
    public static void main(String[] args) throws URISyntaxException, UnsupportedOperationException, IOException {
        // 此处，请输入用户名
        String username = "huangshanongfushan";
        // 此处，请输入对应用户名的密码
        String password = "lm__10121989";
        // 此处，请输入服务的区域信息，参考地址: http://developer.huaweicloud.com/dev/endpoint
        String projectName = "cn-north-1";
        String token = getToken(username, password, projectName);
//        String taskId = requestVideoOcr(token, "f8c0106ad79c489faff83e4579a28aa9", UUID.randomUUID() + "-video-ocr-task", "obs-minist", "11f137137ea06bc3434fc53bdd3f2111.mp4");
//        System.out.println(taskId);
        /*try {
            deleteVideoOcr(token, "f8c0106ad79c489faff83e4579a28aa9", "taskbk883fxs");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        Boolean res = getVideoOcrStatus(token, "f8c0106ad79c489faff83e4579a28aa9", "taskjd5k6hwq");
        System.out.println(res);
    }

}
