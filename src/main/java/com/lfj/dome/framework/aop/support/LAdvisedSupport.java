package com.lfj.dome.framework.aop.support;

import com.lfj.dome.framework.aop.aspect.LAdvice;
import com.lfj.dome.framework.aop.config.LAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class LAdvisedSupport {

    /**
     *  AOP 配置
     */
    private LAopConfig config;

    /**
     *
     */
    private Class<?> targetClass;

    /**
     *
     */
    private Object target;


    private Pattern pointCutClassPattern;


    /**
     *  缓存
     */
    private Map<Method, Map<String, LAdvice>> methodCache = new HashMap<Method, Map<String, LAdvice>>();


    public LAdvisedSupport(LAopConfig aopConfig) {
        this.config = aopConfig;
    }

    private void parse() {
        //把Spring的Excpress变成Java能够识别的正则表达式
        String pointCut = config.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");


        //保存专门匹配Class的正则
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));


        //享元的共享池
        methodCache = new HashMap<Method, Map<String, LAdvice>>();
        //保存专门匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);
        try{
            Class aspectClass = Class.forName(this.config.getAspectClass());
            Map<String,Method> aspectMethods = new HashMap<String, Method>();
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(),method);
            }

            for (Method method : this.targetClass.getMethods()) {
                String methodString = method.toString();
                if(methodString.contains("throws")){
                    methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                }

                Matcher matcher = pointCutPattern.matcher(methodString);
                if(matcher.matches()){
                    Map<String, LAdvice> advices = new HashMap<String, LAdvice>(16);

                    if(!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))){
                        advices.put("before",new LAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectBefore())));
                    }
                    if(!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))){
                        advices.put("after",new LAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfter())));
                    }
                    if(!(null == config.getAspectThrow() || "".equals(config.getAspectThrow()))){
                        LAdvice advice = new LAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectThrow()));
                        advice.setThrowName(config.getThrowTypeClassName());
                        advices.put("afterThrowing",advice);
                    }

                    //跟目标代理类的业务方法和Advices建立一对多个关联关系，以便在Porxy类中获得
                    methodCache.put(method,advices);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public Map<String, LAdvice> getAdvices(Method method, Object o) throws Exception {
        Map<String, LAdvice> cache = methodCache.get(method);
        if (cache == null) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cache = methodCache.get(m);
            this.methodCache.put(m, cache);
        }
        return cache;
    }


    /**
     * 给ApplicationContext首先IoC中的对象初始化时调用，决定要不要生成代理类的逻辑
     * @return b
     */
    public boolean pointCutMath() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }


    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
