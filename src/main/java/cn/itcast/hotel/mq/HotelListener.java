package cn.itcast.hotel.mq;

import cn.itcast.hotel.constants.MqConstants;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description: HotelListener 监听mq队列
 * <br></br>
 * className: HotelListener
 * <br></br>
 * packageName: cn.itcast.hotel.mq
 *
 * @author jinhui-huang
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/8/28 11:32
 */
@Component
public class HotelListener {
    @Autowired
    private IHotelService hotelService;

    /**
     * Description: listenerHotelInsertOrUpdate 监听酒店增或改的消息队列
     * @return void
     * @param id 酒店id
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    @RabbitListener(queues = MqConstants.HOTEL_INSERT_QUEUE)
    public void listenerHotelInsertOrUpdate(Long id){
        hotelService.insertById(id);
    }

    /**
     * Description: listenerHotelDelete 监听酒店删除的消息队列
     * @return void
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    @RabbitListener(queues = MqConstants.HOTEL_DELETE_QUEUE)
    public void listenerHotelDelete(Long id){
        hotelService.deleteById(id);
    }
}
