package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Api(tags ="员工接口")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @ApiOperation("员工登陆")
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @ApiOperation("员工退出")
    @PostMapping("/logout")
    public Result<String> logout() {


        return Result.success();
    }


    @ApiOperation("查询员工讯息 by id")
    @GetMapping("/{id}")
    //有实体的 返回 信息 所以要写明具体类
    public Result<Employee> getById(@PathVariable Long id) {

       return Result.success( employeeService.getById(id));


    }



    @ApiOperation("更改员工信息")
    @PutMapping()
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("update employee : {}", employeeDTO);

        employeeService.update(employeeDTO);
        return Result.success();
    }



    @ApiOperation("员工插入")
    @PostMapping()

    public Result save(@RequestBody EmployeeDTO employeeDTO) {

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        employee.setStatus(StatusConstant.ENABLE);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setPassword(PasswordConstant.DEFAULT_PASSWORD);

        employee.setUpdateUser(   BaseContext.getCurrentId());
        employee.setCreateUser(   BaseContext.getCurrentId());

        employeeService.save(employee);

        return Result.success();


    }

    @ApiOperation("员工分页查询")
    @GetMapping("/page")
    //非json传入
    public Result pageQuery( EmployeePageQueryDTO dto){


        log.info("员工分页查询,参数为 :{}",dto);
       PageResult pageResult=  employeeService.pageQuery(dto);
        return Result.success(pageResult);
    }


    @ApiOperation("员工账号 启用/禁用 ")
    @PostMapping("/status/{status}")
    public Result statusChange(@PathVariable Integer status,Long id){
    log.info("启用或禁用 员工账号 , target status:{}, id:{},",status,id);
        employeeService.statusChange(status,id);//后绪步骤定义
        return Result.success();


    }






}
