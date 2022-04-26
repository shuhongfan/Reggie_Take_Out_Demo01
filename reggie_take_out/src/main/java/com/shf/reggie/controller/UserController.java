package com.shf.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shf.reggie.common.R;
import com.shf.reggie.entity.User;
import com.shf.reggie.service.UserService;
import com.shf.reggie.utils.SMSUtils;
import com.shf.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
//        获取手机号
        String phone = user.getPhone();

        if (!StringUtils.isEmpty(phone)){
//        生成随机的4为验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);
//        调用阿里云提供的短信服务API完成发送短信
//            SMSUtils.sendMessage("瑞吉外卖", "", phone,code);
//        需要将生成的验证码保存到session
            session.setAttribute(phone, code);
            return R.error("短信发送成功");
        }
        return R.success("发送短信失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        log.info(map.toString());
//        获取手机号
        String phone = map.get("phone").toString();
//        获取验证码
        String code = map.get("code").toString();
//        从session中获取保存的验证码
        String codeFromSession = session.getAttribute(phone).toString();
//        进行验证码的比对（页面提交的验证码和session中保存的验证码对比）
        if (codeFromSession!=null && codeFromSession.equals(code)){
//        如果能够对比成功，说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
//        判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            if (user==null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
//            将用户信息保存到session
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
