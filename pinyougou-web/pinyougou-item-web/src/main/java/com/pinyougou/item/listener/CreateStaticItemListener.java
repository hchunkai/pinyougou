package com.pinyougou.item.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.GoodsService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class CreateStaticItemListener implements SessionAwareMessageListener<TextMessage> {

    @Value("${page.dir}")
    private String pageDir;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Reference(timeout = 10000)
    private GoodsService goodsService;

    @Override
    public void onMessage(TextMessage textMessage, Session session) throws JMSException {
        try {
            System.out.println("=====createStaticItemListener创建SKU详情静态页面======");
            String goodsId = textMessage.getText();
            //获取freemarker模板配置类
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("item.ftl");
            //定义数据模板
            Map<String, Object> dataModel = goodsService.getGoods(Long.valueOf(goodsId));
            // 创建输出流
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(pageDir + goodsId + ".html"), "UTF-8");
            //填充模版生成静态的html页面
            template.process(dataModel, writer);
            // 关闭输出流
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
