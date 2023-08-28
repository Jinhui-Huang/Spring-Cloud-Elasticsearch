package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;


    /**
     * Description: search 全文查询的语句
     *
     * @return cn.itcast.hotel.pojo.PageResult
     * @author huian
     * @Date 2023/8/26
     */
    @Override
    public PageResult search(RequestParams params) {
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

    /**
     * Description: filters 聚合查询方法
     *
     * @return java.util.Map<java.lang.String, java.util.List < java.lang.String>>
     * @author huian
     * @Date 2023/8/27
     */
    @Override
    public Map<String, List<String>> filters(RequestParams params) {
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 准备DSL*/
        /*2.1 准备query*/
        buildBasicQuery(params, request);
        /*2.2 设置size*/
        request.source().size(0);
        /*2.3 聚合*/
        buidAggregation(request);
        try {
            /*3. 发出请求*/
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            /*4. 解析结果*/
            Map<String, List<String>> result = new HashMap<>();
            Aggregations aggregations = response.getAggregations();
            /*根据品牌名称, 获取品牌结果*/
            List<String> brandList = getAggByName(aggregations, "brandAgg");
            result.put("品牌", brandList);
            /*根据城市名称, 获取城市结果*/
            List<String> cityList = getAggByName(aggregations, "cityAgg");
            result.put("城市", cityList);
            /*根据星级名称, 获取星级结果*/
            List<String> starList = getAggByName(aggregations, "starAgg");
            result.put("星级", starList);
            return result;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    /**
     * Description: getSuggestion 自动补全实现
     *
     * @return java.util.List<java.lang.String>
     * @author jinhui-huang
     * @Date 2023/8/28
     */
    @Override
    public List<String> getSuggestion(String prefix) {
        /*1. 准备Request*/
        SearchRequest request = new SearchRequest("hotel");
        /*2. 准备DSL*/
        request.source().suggest(new SuggestBuilder().addSuggestion(
                "suggestions",
                SuggestBuilders.completionSuggestion("suggestion")
                        .prefix(prefix)
                        .skipDuplicates(true)
                        .size(10)
        ));
        /*3. 发起请求*/
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            /*4. 解析结果*/
            Suggest suggest = response.getSuggest();
            /*4.1. 根据补全查询名称, 获取补全结果*/
            CompletionSuggestion suggestion = suggest.getSuggestion("suggestions");
            /*4.2. 获取options*/
            List<CompletionSuggestion.Entry.Option> options = suggestion.getOptions();
            /*4.3. 遍历*/
            List<String> list = new ArrayList<>(options.size());
            for (CompletionSuggestion.Entry.Option option : options) {
                String text = option.getText().toString();
                list.add(text);
            }
            return list;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Description: insertById 插入文档数据
     *
     * @return void
     * @author jinhui-huang
     * @Date 2023/8/28
     */
    @Override
    public void insertById(Long id) {
        try {
            /*根据id查询酒店数据*/
            Hotel hotel = getById(id);
            /*转换为文档类型*/
            HotelDoc hotelDoc = new HotelDoc(hotel);
            /*1. 准备Request对象*/
            IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
            /*2. 准备JSON文档*/
            request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
            /*3. 发送请求*/
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Description: deleteById 根据id删除文档
     *
     * @return void
     * @author jinhui-huang
     * @Date 2023/8/28
     */
    @Override
    public void deleteById(Long id) {
        try {
            /*1. 准备Request*/
            DeleteRequest request = new DeleteRequest("hotel", id.toString());
            /*3. 发送请求*/
            client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("201 Created") && !msg.contains("200 OK")) {
                e.printStackTrace();
            }
        }

    }

    private List<String> getAggByName(Aggregations aggregations, String aggName) {
        /*4.1 根据聚合名称获取聚合结果*/
        Terms brandTerm = aggregations.get(aggName);
        /*4.2 获取buckets*/
        List<? extends Terms.Bucket> buckets = brandTerm.getBuckets();
        /*4.3 遍历buckets获取里面的字段值*/
        List<String> brandList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            /*4.4 获取key*/
            String key = bucket.getKeyAsString();
            brandList.add(key);
        }
        /*4.5 放入map*/
        return brandList;
    }

    private void buidAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(100));
        request.source().aggregation(AggregationBuilders
                .terms("cityAgg")
                .field("city")
                .size(100));
        request.source().aggregation(AggregationBuilders
                .terms("starAgg")
                .field("starName")
                .size(100));
    }

    private void buildBasicQuery(RequestParams params, SearchRequest request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        /*关键字搜索*/
        String key = params.getKey();
        if (key == null || "".equals(key)) {
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

    private PageResult handleResponse(SearchRequest request) {
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
