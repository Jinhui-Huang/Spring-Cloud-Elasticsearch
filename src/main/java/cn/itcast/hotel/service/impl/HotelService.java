package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;import org.elasticsearch.common.unit.DistanceUnit;import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;import org.elasticsearch.search.sort.SortOrder;import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;


    /**
     * Description: search 全文查询的语句
     * @return cn.itcast.hotel.pojo.PageResult
     * @author huian
     * @Date 2023/8/26
     * */
    @Override
    public PageResult search(RequestParams params){
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 组织DSL参数*/
        /*如果关键字为空, 则没有符合条件的查询, 直接查询全部, 不然就按关键字查询*/
        /*构建BooleanQuery*/
        buildBasicQuery(params, request);
        /*2.2 分页*/
        int page = params.getPage();
        int size = params.getSize();
        request.source().from((page - 1) * size).size(size);

        /*2.3 排序*/
        String location = params.getLocation();
        if (location != null && !location.equals("")) {
            request.source().sort(SortBuilders
                    .geoDistanceSort("location", new GeoPoint(location))
                    .order(SortOrder.ASC)
                    .unit(DistanceUnit.KILOMETERS));
        }
        /*3. 发送请求, 得到响应结果*/
        return handleResponse(request);
    }

    private static void buildBasicQuery(RequestParams params, SearchRequest request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        /*关键字搜索*/
        String key = params.getKey();
        if(key == null || "".equals(key)) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            /*2.1 关键字搜索*/
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }
        /*条件过滤*/
        /*城市条件*/
        if (params.getCity() != null && !params.getCity().equals("")) {
            boolQuery.filter(QueryBuilders.termQuery("city", params.getCity()));
        }
        /*品牌条件*/
        if (params.getBrand() != null && !params.getBrand().equals("")) {
            boolQuery.filter(QueryBuilders.termQuery("brand", params.getBrand()));
        }
        /*星级条件*/
        if (params.getStartName() != null && !params.getStartName().equals("")) {
            boolQuery.filter(QueryBuilders.termQuery("startName", params.getStartName()));
        }
        /*价格条件*/
        if (params.getMinPrice() != null && params.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(params.getMinPrice()).lte(params.getMaxPrice()));
        }
        /*2. 算分查询*/
        FunctionScoreQueryBuilder functionScoreQuery =
                QueryBuilders.functionScoreQuery(
                        /*原始查询, 相关性算分查询*/
                        boolQuery,
                        /*function score的数组*/
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                                /*其中的一个function score元素*/
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        /*过滤条件*/
                                        QueryBuilders.termQuery("isAD", true),
                                        /*算分函数*/
                                        ScoreFunctionBuilders.weightFactorFunction(10)
                                )
                        });

        request.source().query(functionScoreQuery);
    }

    private PageResult handleResponse(SearchRequest request){
        /*3. 发送请求, 得到响应结果*/
        try {
            SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
            /*4. 解析响应结果*/
            SearchHits hits = searchResponse.getHits();
            /*获取总条数*/
            long total = hits.getTotalHits().value;
            /*4.2 查询的结果数组*/
            SearchHit[] searchHits = hits.getHits();
            List<HotelDoc> hotels = new ArrayList<>();
            for (SearchHit searchHit : searchHits) {
                /*获取文档source*/
                String json = searchHit.getSourceAsString();
                /*反序列化*/
                HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
                /*获取排序值*/
                Object[] sortValues = searchHit.getSortValues();
                if (sortValues.length > 0) {
                    Object sortValue = sortValues[0];
                    hotelDoc.setDistance(sortValue);
                }
                hotels.add(hotelDoc);
            }
            /*封装返回*/
            return new PageResult(total, hotels);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                e.printStackTrace();
            }
        }
        return new PageResult();
    }
}
