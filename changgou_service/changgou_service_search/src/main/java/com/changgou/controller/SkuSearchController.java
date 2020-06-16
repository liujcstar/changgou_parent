package com.changgou.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.service.SkuSearchService;
import com.changgou.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RequestMapping("/search")
@Controller
public class SkuSearchController {

    @Autowired
    private SkuSearchService skuSearchService;


    //创建索引库
    @PutMapping("/create")
    @ResponseBody
    public Result createMapping() {
        skuSearchService.createMapping();

        return new Result(true, StatusCode.OK, "创建索引库成功");
    }

    //预热索引库
    @PutMapping("/importAll")
    @ResponseBody
    public Result importAll() {
        skuSearchService.importAll();

        return new Result(true, StatusCode.OK, "导入数据成功");
    }


    //ES高级检索
    @GetMapping("/search")
    @ResponseBody
    public Result search(@RequestParam Map<String, String> map) {

//        this.handleSearchMap(map);

        Map<String, Object> mapResult = skuSearchService.search(map);

        return new Result(true, StatusCode.OK, "检索成功", mapResult);

    }


    /**
     * 页面静态化技术
     *
     * @return
     */
    @GetMapping("/thymeleaf")
    public String list(@RequestParam Map<String, String> searchMap, Model model) {


        //返回查询条件数据
        model.addAttribute("searchMap", searchMap);
        Map<String, Object> resultMap = skuSearchService.search(searchMap);


        //封装结果对象
        model.addAttribute("resultMap", resultMap);


        //封装分页对象
        Long total = Long.parseLong(resultMap.get("total").toString()) ;
        Integer pageNum =  Integer.parseInt(resultMap.get("pageNum").toString());
        Integer pageSize = Integer.parseInt(resultMap.get("pageSize").toString()) ;
        Page<SkuInfo> page = new Page<SkuInfo>(total,pageNum,pageSize);

        model.addAttribute("page",page);




        //拼接当前条件查询的url
        StringBuilder url = new StringBuilder("/search/thymeleaf");
        if (searchMap != null && searchMap.size() > 0) {
            url.append("?");

            for (String key : searchMap.keySet()) {

                if (!key.equals("pageNum")&&!key.equals("sortFile")&&!key.equals("sortRule")){

                    url.append(key).append("=").append(searchMap.get(key)).append("&");

                }
            }

            String substring = url.toString().substring(0, url.length() - 1);

            model.addAttribute("url",substring);
        }


        return "search";
    }


    private void handleSearchMap(Map<String, String> searchMap) {
        Set<Map.Entry<String, String>> entries = searchMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            if (entry.getKey().startsWith("spec_")) {
                searchMap.put(entry.getKey(), entry.getValue().replace("+", "%2B"));
            }
        }
    }


}
