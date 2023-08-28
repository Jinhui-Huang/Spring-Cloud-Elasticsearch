package cn.itcast.hotel;

import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * className HotelDemoTests
 * packageName cn.itcast.hotel
 * Description HotelDemoTests
 *
 * @author jinhui-huang
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/8/27 15:35
 */
@SpringBootTest
public class HotelDemoTests {

    @Autowired
    private IHotelService hotelService;

    @Test
    void contextLoads() {
        RequestParams params = new RequestParams();
        Map<String, List<String>> filters = hotelService.filters(params);
        System.out.println(filters);
    }
}
