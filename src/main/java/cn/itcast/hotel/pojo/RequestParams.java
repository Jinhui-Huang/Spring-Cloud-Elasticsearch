package cn.itcast.hotel.pojo;

import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * className RequestParams 接收前端的请求参数
 * packageName cn.itcast.hotel.pojo
 * Description RequestParams
 * ibus安装最新版
 * @author huian
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/8/26 09:42
 */
@Data
public class RequestParams {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String brand;
    private String startName;
    private String city;
    private Integer minPrice;
    private Integer maxPrice;
    private String location;
}
