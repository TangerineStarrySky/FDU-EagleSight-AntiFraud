package com.example.smsdetection.utils;// 建议dashscope SDK的版本 >= 2.12.0
import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.lang.System;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.example.smsdetection.model.AppViewModel;
import com.example.smsdetection.model.ChatCallback;

public class ChatClient {
    public static final int QWEN0_5b = 0;
    public static final int QWEN1_5b = 1;
    public static final int QWEN3b = 2;
    public static final String[] MODELS = {"qwen2.5-0.5b-instruct", "qwen2.5-1.5b-instruct", "qwen2.5-3b-instruct"};

//    private AppViewModel appViewModel;
//
//    public ChatClient(Application application) {
//        appViewModel = new AppViewModel(application);
//        Log.d("DEBUG", "ChatClient: new AppViewModel");
//        appViewModel.getModelList().get(0).startChat();
//        Log.d("DEBUG", "ChatClient: startChat success");
//    }


    public String callWithMessageOnDevice(String input){
//        Log.d("DEBUG", "callWithMessageOnDevice: enter");
//        try {
//            appViewModel.getChatState().myChat(input, new ChatCallback() {
//                @Override
//                public void onResult(@NonNull String result) {
//                    Log.d("DEBUG", "callWithMessageOnDevice onResult: "+result);
//                }
//            });
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        return "成功";
    }


    public String callWithMessage(String input, int model_id) throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个反诈领域的专家，你会接收到一段短信息，请你判断该信息是否为诈骗短信，返回\"是\"或者\"否\"。")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(input)
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
//                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .apiKey("sk-965fb4ea3c864b048f26b4f5a15851a3")
                .model(MODELS[model_id])
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        GenerationResult result = gen.call(param);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

    public String callForDetail(String input, int model_id) throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个反诈领域的专家，你会收到一条诈骗短信，请你描述该信息可能属于的诈骗类别并阐述其诈骗套路。" +
                        "(诈骗类别主要有：刷单返利类诈骗，虚假网络投资理财类诈骗，虚假网络贷款类诈骗，冒充电商物流客服类诈骗，冒充公检法类诈骗，" +
                        "虚假征信类诈骗，虚假购物、服务类诈骗，冒充领导、熟人类诈骗，网络游戏产品虚假交易类诈骗，婚恋、交友类诈骗)")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(input)
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
//                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .apiKey("sk-965fb4ea3c864b048f26b4f5a15851a3")
                .model(MODELS[model_id])
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        GenerationResult result = gen.call(param);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

//    public static void main(String[] args) {
//        try {
//            String neg_text = "新台新运,牛年牛气,100送288直达,500送688直达,一水无限制,真诚相邀,机会不容错过,加Q2747491745";
//            String pos_text = "我为什么觉得花千骨这部片是一场巨大的审美钓鱼呢…一点格调都没有";
//            String result = callWithMessage(neg_text, ChatClient.QWEN3b);
//            System.out.println(result);
//            String detail = callForDetail(neg_text, ChatClient.QWEN3b);
//            System.out.println(detail);
//            String result2 = callWithMessage(pos_text, ChatClient.QWEN3b);
//            System.out.println(result2);
//        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
//            // 使用日志框架记录异常信息
//            System.err.println("An error occurred while calling the generation service: " + e.getMessage());
//        }
//        String neg_text = "新台新运,牛年牛气,100送288直达,500送688直达,一水无限制,真诚相邀,机会不容错过,加Q2747491745";
//        String result = callWithMessageOnDevice(neg_text);
//        System.out.println(result);
//        System.exit(0);
//    }
}