package cn.jiguang.community.base.data.service;

import cn.jiguang.community.base.data.Application;
import cn.jiguang.community.base.data.model.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = Application.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testUpdateByUserId() {

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(31L);
        userEntity.setPassword("xdx");
        userService.updateByUserId(userEntity);
    }

    @Test
    public void testBlock() {
        Long userId = 99L;
        userService.blockIntroduce(userId);
        UserEntity user =  userService.getByUserId(userId);
        Assert.assertEquals("", user.getIntroduce());
    }

}
