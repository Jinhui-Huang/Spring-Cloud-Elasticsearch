package cn.itcast.hotel.constants;

/**
 * className MqConstants
 * packageName cn.itcast.hotel.constants
 * Description MqConstants
 *
 * @author jinhui-huang
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/8/28 10:38
 */
public class MqConstants {
    /**
     * Description: 交换机
     * @return 
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    public final static String HOTEL_EXCHANGE = "hotel.topic";
    
    /**
     * Description: 监听新增和修改的队列
     * @return 
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    public final static String HOTEL_INSERT_QUEUE = "hotel.insert.queue";
    
    /**
     * Description: 监听删除的队列
     * @return 
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    public final static String HOTEL_DELETE_QUEUE = "hotel.delete.queue";
    
    /**
     * Description: 新增或修改的RoutingKey
     * @return 
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    public final static String HOTEL_INSERT_KEY = "hotel.insert";

    /**
     * Description: 删除的RotingKey
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    public final static String HOTEL_DELETE_KEY = "hotel.delete";
}
