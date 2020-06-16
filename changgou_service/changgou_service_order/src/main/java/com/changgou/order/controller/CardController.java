package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.config.TokenDecode;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequestMapping("/cart")
@RestController
public class CardController {

    @Autowired
    private TokenDecode tokenDecode;



    @Autowired
    private CartService cartService;

    @GetMapping("/addCart")
    public Result addCard(@RequestParam("skuId")String skuId,@RequestParam("num") Integer num){
        //动态用户名
        String username = tokenDecode.getUserInfo().get("username");
        //静态用户名
//        String username = "heima";
        cartService.addCard(skuId,num ,username );

        return new Result(true, StatusCode.OK,"添加购物车成功");
    }


    /**
     * 查询购物车
     * @return
     */
    @GetMapping("/list")
    public Result<Map> list(HttpServletRequest request){

        String header = request.getHeader("Authorization");
        String jwt = header.split(" ")[1];
        Map<String, String> tokenMap = tokenDecode.dcodeToken(jwt);

        String username1 = tokenMap.get("username");

        //动态用户名
        //String username = tokenDecode.getUserInfo().get("username");

        //静态用户名
//        String username = "heima";

       Map map = cartService.list(username1);

        return new Result<Map>(true,StatusCode.OK,"查询成功",map);
    }





}
