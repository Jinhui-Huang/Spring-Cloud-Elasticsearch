package cn.itcast.hotel.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * className PageResult 分页查询结果
 * packageName cn.itcast.hotel.pojo
 * Description PageResult
 *
 * @author huian
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/8/26 09:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult {
    private Long total;
    private List<HotelDoc> hotels;
}
