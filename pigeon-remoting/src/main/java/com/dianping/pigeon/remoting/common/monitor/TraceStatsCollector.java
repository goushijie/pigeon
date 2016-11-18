//package com.dianping.pigeon.remoting.common.monitor;
//
//import com.dianping.pigeon.config.ConfigManagerLoader;
//import com.dianping.pigeon.monitor.ApplicationStatsData;
//import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
//import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
//import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
//import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
//
///**
// * @author qi.yin
// *         2016/11/01  下午4:41.
// */
//public class TraceStatsCollector {
//
//    private static final String appName = ConfigManagerLoader.getConfigManager().getAppName();
//
//    private static final ApplicationStatsData statsData = new ApplicationStatsData(appName);
//
//    public static void updateProvideCount(ProviderContext context) {
//        InvocationRequest request = context.getRequest();
//        statsData.updateProviderTotalCount(context.getMethodKey(), request.getApp());
//    }
//
//    public static void addProvideData(ProviderContext context) {
//        InvocationRequest request = context.getRequest();
//        statsData.addProviderData(context.getMethodKey(), request.getApp(), (byte) request.getCallType(),
//                request.getSerialize(), request.getTimeout());
//    }
//
//    public static void updateProvideData(ProviderContext context, long startMillis, boolean isSuccess) {
//        long elapsed = System.currentTimeMillis() - startMillis;
//        InvocationRequest request = context.getRequest();
//        statsData.updateProviderData(context.getMethodKey(), request.getApp(), elapsed, isSuccess);
//    }
//
//    public static void beforeProvide(ProviderContext context) {
//        statsData.startProvider(context.getMethodKey());
//    }
//
//    public static void afterProvide(ProviderContext context) {
//        statsData.completeProvider(context.getMethodKey());
//    }
//
//    public static void andInvokeData(InvokerContext context) {
//        InvokerConfig config = context.getInvokerConfig();
//        String methodName = context.getMethodName();
//        byte callMethod = config.getCallMethod(methodName);
//        int timeout = config.getTimeout(methodName);
//        byte serialize = config.getSerialize();
//
//        statsData.addInvokerData(context.getMethodKey(), callMethod, serialize, timeout);
//    }
//
//    public static void updateInvokeData(InvokerContext context, long startMillis, boolean isSuccess) {
//        long elapsed = System.currentTimeMillis() - startMillis;
//
//        statsData.updateInvokerData(context.getMethodKey(), elapsed, isSuccess);
//    }
//
//    public static void updateInvokeCount(InvokerContext context) {
//        statsData.updateInvokerTotalCount(context.getMethodKey());
//    }
//
//    public static void beforeInvoke(InvokerContext context) {
//        statsData.startInvoker(context.getMethodKey());
//    }
//
//    public static void afterInvoke(InvokerContext context) {
//        statsData.completeInvoker(context.getMethodKey());
//    }
//
//    public static ApplicationStatsData getStatisData() {
//        return statsData;
//    }
//
//}
