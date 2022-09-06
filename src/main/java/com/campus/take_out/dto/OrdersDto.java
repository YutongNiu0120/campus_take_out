package com.campus.take_out.dto;

import com.campus.take_out.entity.OrderDetail;
import com.campus.take_out.entity.Orders;
import lombok.Data;
import org.springframework.core.annotation.Order;

import java.util.List;

@Data
public class OrdersDto extends Orders {
    private List<OrderDetail> orderDetails;
//    private int sumNum;
}
