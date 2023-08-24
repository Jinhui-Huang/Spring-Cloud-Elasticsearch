package cn.itcast.hotel;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static cn.itcast.hotel.constants.HotelConstants.MAPPING_TEMPLATE;

/**
 * className HotelIndexTest
 * packageName cn.itcast.hotel
 * Description HotelIndexTest
 *
 * @author huian
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/8/23 12:37
 */

public class HotelIndexTest {
    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    private RestHighLevelClient client;

    @Test
    void testInit() {
        System.out.println(client);
    }

    /**
     * Description: testCreateHotelIndex 创建索引库
     *
     * @return void
     * @author huian
     * @Date 2023/8/23
     */
    @Test
    void testCreateHotelIndex() throws IOException {
        /*1. 创建Request对象*/
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        /*2. 请求参数, DSL语句(Json风格)*/
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        /*3. 发送请求*/
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    /**
     * Description: testDeleteHotelIndex
     * @return void
     * @author huian
     * @Date 2023/8/23
     * */
    @Test
    void testDeleteHotelIndex() throws IOException {
        /*1. 创建Request对象*/
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        /*2. 发送请求*/
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testExistsHotelIndex() throws IOException {
        /*1. 创建Request对象*/
        GetIndexRequest request = new GetIndexRequest("hotel");
        /*2. 发送请求*/
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        /*3. 输出*/
        System.err.println(exists ? "索引库已经存在!" : "索引库不存在!");
    }

    @BeforeEach
    void setUp() {
        /*初始化客户端设置elasticsearch的http地址*/
        this.credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "elastic"));
        this.client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://127.0.0.1:9200")).setHttpClientConfigCallback(
                httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider)));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

}
