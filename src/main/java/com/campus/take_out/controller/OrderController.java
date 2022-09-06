package com.campus.take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.take_out.common.BaseContext;
import com.campus.take_out.common.R;
import com.campus.take_out.dto.OrdersDto;
import com.campus.take_out.entity.OrderDetail;
import com.campus.take_out.entity.Orders;
import com.campus.take_out.service.OrderDetailService;
import com.campus.take_out.service.OrderService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/page")
    public R<Page> page(@RequestParam Map map) {
        int page = Integer.parseInt((String) map.get("page"));
        int pageSize = Integer.parseInt((String) map.get("pageSize"));
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        if (map.containsKey("number")) {
            Long number = Long.parseLong((String) map.get("number"));
            queryWrapper.eq(Orders::getNumber, number);
        }

        if (map.containsKey("beginTime") && map.containsKey("endTime")) {
            try {
                Date beginTime = DateUtils.parseDate((String) map.get("beginTime"), "yyyy-MM-dd HH:mm:ss");
                Date endTime = DateUtils.parseDate((String) map.get("endTime"), "yyyy-MM-dd HH:mm:ss");
                queryWrapper.between(Orders::getOrderTime, beginTime, endTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        orderService.page(pageInfo, queryWrapper);

//        添加用户名
        List<Orders> records = pageInfo.getRecords();
        records.stream().map(item -> {
            if (item.getUserName() == null) {
                item.setUserName("未命名用户");
            }
            return item;
        }).collect(Collectors.toList());


        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> dispatch(@RequestBody Map map) {
        log.info(String.valueOf(map));

        Long id = Long.parseLong((String) map.get("id"));
        int status = (int) map.get("status");
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, id);
        updateWrapper.set(Orders::getStatus, status);
        orderService.update(updateWrapper);

        return R.success("订单状态更改成功");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        orderService.page(pageInfo, queryWrapper);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> ordersDtoList = records.stream().map(item -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> list = orderDetailService.list(orderDetailLambdaQueryWrapper);
            ordersDto.setOrderDetails(list);
//            int sumNum = 0;
//            for (OrderDetail o : list) {
//                sumNum += o.getNumber();
//            }
//            ordersDto.setSumNum(sumNum);
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtoList);

        return R.success(ordersDtoPage);
    }

}
