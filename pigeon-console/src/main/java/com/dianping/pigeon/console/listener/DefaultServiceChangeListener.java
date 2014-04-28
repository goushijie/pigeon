/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.console.listener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.dianping.dpsf.exception.ServiceException;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.config.ProviderConfig;
import com.dianping.pigeon.remoting.provider.listener.ServiceChangeListener;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;

public class DefaultServiceChangeListener implements ServiceChangeListener {

	private static final Logger logger = LoggerLoader.getLogger(DefaultServiceChangeListener.class);

	private static ConfigManager configManager = ExtensionLoader.getExtension(ConfigManager.class);

	private Set<String> publishedUrls = new HashSet<String>();

	private Map<String, NotifyEvent> failedNotifyEvents = new ConcurrentHashMap<String, NotifyEvent>();
	
	private static ThreadPool failureListenerThreadPool = new DefaultThreadPool("pigeon-notify-failure-listener");

	public DefaultServiceChangeListener() {
		failureListenerThreadPool.execute(new NotifyFailureListener(this));
	}
	
	public Map<String, NotifyEvent> getFailedNotifyEvents() {
		return failedNotifyEvents;
	}

	private HttpClient getHttpClient() {
		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxTotalConnections(300);
		params.setDefaultMaxConnectionsPerHost(50);
		params.setConnectionTimeout(3000);
		params.setTcpNoDelay(true);
		params.setSoTimeout(3000);
		params.setStaleCheckingEnabled(true);
		connectionManager.setParams(params);
		HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionManager(connectionManager);

		return httpClient;
	}

	@Override
	public synchronized void notifyServicePublished(ProviderConfig<?> providerConfig) throws ServiceException {
		if (!publishedUrls.contains(providerConfig.getUrl())) {
			logger.info("start to notify service published:" + providerConfig);
			notifyServiceChange("publish", providerConfig);
			publishedUrls.add(providerConfig.getUrl());
			logger.info("succeed to notify service published:" + providerConfig);
		}
	}

	public synchronized void notifyServiceChange(String action, ProviderConfig<?> providerConfig)
			throws ServiceException {
		String managerAddress = configManager.getStringValue(Constants.KEY_MANAGER_ADDRESS,
				Constants.DEFAULT_MANAGER_ADDRESS);
		StringBuilder url = new StringBuilder();
		url.append("http://").append(managerAddress).append("/service/").append(action);
		url.append("?env=").append(configManager.getEnv()).append("&id=3&updatezk=false&service=");
		url.append(providerConfig.getUrl());
		String group = providerConfig.getServerConfig().getGroup();
		if (StringUtils.isBlank(group)) {
			group = Constants.DEFAULT_GROUP;
		}
		url.append("&group=").append(group);
		url.append("&ip=").append(configManager.getLocalIp());
		url.append("&port=").append(providerConfig.getServerConfig().getPort());

		failedNotifyEvents.remove(providerConfig.getUrl());
		boolean isSuccess = doNotify(url.toString());
		if (!isSuccess) {
			NotifyEvent event = new NotifyEvent();
			event.setNotifyUrl(url.toString());
			failedNotifyEvents.put(providerConfig.getUrl(), event);
		}
	}

	synchronized boolean doNotify(String url) {
		HttpClient httpClient = getHttpClient();
		GetMethod getMethod = null;
		String response = null;
		logger.info("service change notify url:" + url);
		try {
			getMethod = new GetMethod(url);
			httpClient.executeMethod(getMethod);
			if (getMethod.getStatusCode() >= 300) {
				throw new ServiceException("Did not receive successful HTTP response: status code = "
						+ getMethod.getStatusCode() + ", status message = [" + getMethod.getStatusText() + "]");
			}
			response = getMethod.getResponseBodyAsString();
		} catch (Throwable t) {
			logger.error("error while notifying service change to url:" + url, t);
		} finally {
			if (getMethod != null) {
				getMethod.releaseConnection();
			}
		}
		boolean isSuccess = false;
		if (response != null && response.startsWith("0")) {
			isSuccess = true;
		}
		if (!isSuccess) {
			logger.error("error while notifying service change to url:" + url.toString() + ", response:" + response);
		}
		return isSuccess;
	}

	@Override
	public synchronized void notifyServiceUnpublished(ProviderConfig<?> providerConfig) throws ServiceException {
		if (publishedUrls.contains(providerConfig.getUrl())) {
			logger.info("start to notify service unpublished:" + providerConfig);
			try {
				notifyServiceChange("unpublish", providerConfig);
				logger.info("succeed to notify service unpublished:" + providerConfig);
			} catch (ServiceException t) {
				logger.warn(t.getMessage());
			}
			publishedUrls.remove(providerConfig.getUrl());
		}
	}

}