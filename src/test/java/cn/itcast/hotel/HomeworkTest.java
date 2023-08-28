package cn.itcast.hotel;

import org.junit.jupiter.api.Test;

/**
 * className Test
 * packageName cn.itcast.hotel
 * Description Test
 *
 * @author jinhui-huang
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/8/27 17:50
 */
public class HomeworkTest {

    @Test
    void test(){
        int i = 0, sum = 0;
		while (i <= 10) {
			sum += i;
			i++;
			if (i % 2 == 0)
				continue;
		}
		System.out.println(sum);

    }

}
