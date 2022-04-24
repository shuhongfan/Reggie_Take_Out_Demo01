package com.shf.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shf.reggie.common.R;
import com.shf.reggie.entity.Employee;
import com.shf.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping(value = "/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
//        1.将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

//        2.根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

//        3.如果没有查询到则返回登录失败的结果
        if (emp==null){
            return R.error("登录失败");
        }


//        4.密码比对，如果不一致则返回登录失败的结果
        if (!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

//        5.查看员工状态，如果为已禁用状态，则返回员工已禁用
        if (emp.getStatus()==0){
            return R.error("账号已禁用");
        }

//        6.登录成功，将员工id存入session并返回成功
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 退出登录
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
//        请求Session中保存的当前登录员工的Id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee);

//        设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));

//        设置创建和更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

//        获得当前登录用户id
        Long empId = (Long) request.getSession().getAttribute("employee");
//        设置创建和更新用户
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

//        调度service创建员工
        employeeService.save(employee);

        return R.success("创建员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
//        构造分页构造器
        Page pageInfo = new Page(page, pageSize);

//        条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();

//        添加过滤条件
        queryWrapper.like(!StringUtils.isEmpty(name), Employee::getName,name);

//        添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

//        执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info("根据id修改员工:{}",employee.toString());
//        设置更新时间
        employee.setUpdateTime(LocalDateTime.now());
//        设置更新用户
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(empId);

//        调用service更新
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id)
    {
        log.info("根据id查询员工信息");
        Employee employee = employeeService.getById(id);
        if (employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
}
