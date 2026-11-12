package com.example.demo.util;

/**
 * 结果封装类
 */
public class ResultUtils {



    public static JsonResult buildJsonResult(){
        return new JsonResult();
    }

    public static JsonResult ok(String msg,Object data){
        return buildJsonResult().setCode(200).setMessage(msg).put("data",data);
    }
    public static JsonResult ok(Object data){
        return ok("操作成功",data);
    }

    public static JsonResult ok(){
        return ok(null);
    }
    public static JsonResult error(int code,String msg,Object data){
        return buildJsonResult().setCode(code).setMessage(msg).put("data",data);
    }
    public static JsonResult error(int code,String msg){
        return error(code,msg,null);
    }
    public static JsonResult error(String msg,Object data){
        return error(500,msg,data);
    }
    public static JsonResult error(String msg){
        return error(msg,null);
    }
    public static JsonResult errorParameter(){
        return error("参数错误",null);
    }

    public static JsonResult error(){
        return error("操作失败");
    }







}
