package cn.itcast.hotel.config;

import cn.itcast.hotel.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description: MqConfig配置交换机
 * <br></br>
 * className: MqConfig
 * <br></br>
 * packageName: cn.itcast.hotel.config
 *
 * @author jinhui-huang
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/8/28 11:09
 */
@Configuration
public class MqConfig {
    /**
     * Description: topicExchange 定义交换机
     * <br></br>
     * @return org.springframework.amqp.core.TopicExchange
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(MqConstants.HOTEL_EXCHANGE, true, false);
    }

    /**
     * Description: insertQueue 数据发生插入或者更新时通知消息的队列
     * @return org.springframework.amqp.core.Queue
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    @Bean
    public Queue insertQueue(){
        return new Queue(MqConstants.HOTEL_INSERT_QUEUE, true);
    }

    /**
     * Description: deleteQueue 数据发生删除时通知消息的队列
     * @return org.springframework.amqp.core.Queue
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    @Bean
    public Queue deleteQueue(){
        return new Queue(MqConstants.HOTEL_DELETE_QUEUE, true);
    }

    /**
     * Description: insertQueueBinding 绑定交换机和数据发生插入或者更新时通知消息的队列
     * @return org.springframework.amqp.core.Binding
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    @Bean
    public Binding insertQueueBinding(){
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(MqConstants.HOTEL_INSERT_KEY);
    }

    /**
     * Description: deleteQueueBinding 绑定交换机和数据发生删除时通知消息的队列
     * @return org.springframework.amqp.core.Binding
     * @author jinhui-huang
     * @Date 2023/8/28
     * */
    @Bean
    public Binding deleteQueueBinding(){
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(MqConstants.HOTEL_DELETE_KEY);
    }
}
