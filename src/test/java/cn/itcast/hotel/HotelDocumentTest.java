package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

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
@SpringBootTest
@SuppressWarnings("all")
public class HotelDocumentTest {
    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    @Autowired
    private IHotelService hotelService;
    private RestHighLevelClient client;

    @Test
    void testAddDocument() throws IOException {
        /*根据id查询酒店数据*/
        Hotel hotel = hotelService.getById(61083L);
        /*转换为文档类型*/
        HotelDoc hotelDoc = new HotelDoc(hotel);
        /*1. 准备Request对象*/
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
        /*2. 准备JSON文档*/
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        /*3. 发送请求*/
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                throw e;
            }
        }
    }

    @Test
    void testGetDocumentById() throws IOException {
        /*1. 准备Request*/
        GetRequest request = new GetRequest("hotel", "61083");
        /*2. 发送请求, 得到响应*/
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        /*3. 解析响应结果*/
        String json = response.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    @Test
    void testUpdateDocumentById() throws IOException {
        /*1. 准备Request*/
        UpdateRequest request = new UpdateRequest("hotel", "61083");
        /*2. 准备请求参数*/
        request.doc(
                "price", "952",
                "starName", "四钻"
        );
        /*3. 发送请求*/
        try {
            client.update(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                throw e;
            }
        }
    }

    @Test
    void testDeleteDocumentById() throws IOException {
        /*1. 准备Request*/
        DeleteRequest request = new DeleteRequest("hotel", "61038");
        /*3. 发送请求*/
        try {
            client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                throw e;
            }
        }
    }

    @Test
    void testBulk() throws IOException {
        /*批量查询酒店数据*/
        List<Hotel> hotels = hotelService.list();

        /*1. 创建Bulk请求*/
        BulkRequest request = new BulkRequest();
        /*2. 准备参数, 添加多个新增的Request*/
        /*转换为稳当类型HotelDoc*/
        for (Hotel hotel : hotels) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            /*创建新增文档的Request对象*/
            request.add(new IndexRequest("hotel").id(hotelDoc.getId().toString()).
                    source(JSON.toJSONString(hotelDoc), XContentType.JSON));
        }
        /*3. 发送请求*/
        try {
            client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                throw e;
            }
        }

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
