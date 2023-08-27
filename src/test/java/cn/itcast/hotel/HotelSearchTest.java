package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;import org.elasticsearch.search.sort.SortOrder;import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;import org.springframework.util.CollectionUtils;

import java.io.IOException;import java.util.Map;

/**
 * className HotelSearchTest
 * packageName cn.itcast.hotel
 * Description HotelSearchTest
 *
 * @author huian
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/8/24 19:20
 */
@SpringBootTest
public class HotelSearchTest {
    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    private final Logger logger = LoggerFactory.getLogger(HotelSearchTest.class);
    private RestHighLevelClient client;


    @Test
    void testMatchAll() throws IOException {
        int page = 1, size = 5;
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 组织DSL参数*/
        request.source().query(QueryBuilders.matchAllQuery());
        /*分页*/
        request.source().from(page).size(size);
        /*价格排序*/
        request.source().sort("price", SortOrder.ASC);
        /*3. 发送请求, 得到响应结果*/
        handleResponse(request);
    }

    @Test
    void testHighLighter() throws IOException {
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 组织DSL参数*/
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        /*高亮显示*/
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        /*3. 发送请求, 得到响应结果*/
        try {
            SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
            /*4. 解析响应结果*/
            /*logger.info(searchResponse);*/
            SearchHits hits = searchResponse.getHits();
            /*4.1 查询的总条数*/
            long total = hits.getTotalHits().value;
            logger.info("共搜索到" + total + "条数据");
            /*4.2 查询的结果数组*/
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                /*获取文档source*/
                String json = searchHit.getSourceAsString();
                /*反序列化*/
                HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
                Map<String,HighlightField> highlightFields = searchHit.getHighlightFields();
                if (!CollectionUtils.isEmpty(highlightFields)) {
                    /*获取高亮字段*/
                    HighlightField highlightField = highlightFields.get("name");
                    if (highlightField != null) {
                        String name = highlightField.getFragments()[0].string();
                        hotelDoc.setName(name);
                    }
                }
                logger.info("hotelDoc=" + hotelDoc);
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                throw e;
            }
        }
    }

    /**
     * Description: testMatchQuery单字段查询
     *
     * @return void
     * @author huian
     * @Date 2023/8/24
     */
    @Test
    void testMatchQuery() throws IOException {
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 组织DSL参数*/
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        handleResponse(request);
    }

    /**
     * Description: testMultiMatchQuery多字段查询
     *
     * @return void
     * @author huian
     * @Date 2023/8/24
     */
    @Test
    void testMultiMatchQuery() throws IOException {
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 组织DSL参数*/
        request.source().query(QueryBuilders.multiMatchQuery("如家", "name", "business"));
        /*3. 发送请求, 得到响应结果*/
        handleResponse(request);
    }

    /**
     * Description: testTermQuery词条查询
     * @return void
     * @author huian
     * @Date 2023/8/24
     * */
    @Test
    void testTermQuery() throws IOException {
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 组织DSL参数*/
        request.source().query(QueryBuilders.termQuery("city", "深圳"));
        /*3. 发送请求, 得到响应结果*/
        handleResponse(request);
    }

    /**
     * Description: rangeQuery范围查询
     * @return void
     * @author huian
     * @Date 2023/8/24
     * */
    @Test
    void rangeQuery() throws IOException {
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 组织DSL参数*/
        request.source().query(QueryBuilders.rangeQuery("price").gte(100).lte(150));
        /*3. 发送请求, 得到响应结果*/
        handleResponse(request);
    }

    @Test
    void testBoolQuery() throws IOException {
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 创建布尔查询*/
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        /*添加must条件*/
        boolQuery.must(QueryBuilders.termQuery("city", "上海"));
        /*添加filer条件*/
        boolQuery.filter(QueryBuilders.rangeQuery("price").lte(250));
        /*3. 组织DSL参数*/
        request.source().query(boolQuery);
        /*3. 发送请求, 得到响应结果*/
        handleResponse(request);
    }

    private void handleResponse(SearchRequest request) throws IOException {
        /*3. 发送请求, 得到响应结果*/
        try {
            SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
            /*4. 解析响应结果*/
            /*logger.info(searchResponse);*/
            SearchHits hits = searchResponse.getHits();
            /*4.1 查询的总条数*/
            long total = hits.getTotalHits().value;
            logger.info("共搜索到" + total + "条数据");
            /*4.2 查询的结果数组*/
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                /*获取文档source*/
                String json = searchHit.getSourceAsString();
                /*反序列化*/
                HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
                logger.info("hotelDoc=" + hotelDoc);
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                throw e;
            }
        }
    }

    @Test
    void testAggregation() throws IOException {
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 准备DSL*/
        /*2. 1设置size*/
        request.source().size(0);
        /*2. 2聚合*/
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(10));
        try {
            /*3. 发出请求*/
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            /*4. 解析结果*/
            System.out.println(response);


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
