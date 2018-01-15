package com.httpLient.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class HttpClientUtil {

	final static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

     static {
    	 InputStream inStream= HttpClientUtil.class.getResourceAsStream("/url.properties");
    	 Properties pres = new Properties();
    	 try {
			pres.load(inStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			 logger.error("未获取到资源文件路径");
			e.printStackTrace();
		}
    
     }
	
     public static boolean validateResultData(JSONObject result) throws Exception {
 		if (result.getInteger("Code") == null || 0 != result.getInteger("Code")) {
 			logger.error("请求失败，可能缺少参数{}", result.toJSONString());
 			throw new Exception("上传失败" + result.toJSONString());

 			// 上传成功
 		} else {

 			return true;
 		}

 	}

     /**
      * demo
      */
	public static JSONObject getUploadCode() throws Exception {
		Map<String, Object> reqParams = new HashMap<>();
		Object appid = null;
		reqParams.put("appid", appid);
		Object upload_user_id = null;
		reqParams.put("upload_user_id", upload_user_id);
		Object download_url_type = null;
		reqParams.put("download_url_type", download_url_type);
		Object filehash = null;
		reqParams.put("filehash", filehash);
		Object filename = null;
		reqParams.put("filename", filename);
		String uplodeCodeUrl = null;
		CloseableHttpResponse response = HttpClientUtil.postRequestNoFile(uplodeCodeUrl, reqParams);
		JSONObject result = HttpClientUtil.responseNoHasfile(response);
		HttpClientUtil.validateResultData(result);

		return result;

	}



	/**
	 * 2.发起POST请求
	 * 
	 * @desc ：
	 * 
	 * @param url
	 *            请求url
	 * @param data
	 *            请求参数（json）
	 * @return
	 * @throws Exception
	 *             JSONObject
	 */
	private static JSONObject doPost(String url, Object data) throws Exception {
		// 1.生成一个请求
		HttpPost httpPost = new HttpPost(url);

		// 2.配置请求属性
		// 2.1 设置请求超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(100000).setConnectTimeout(100000).build();
		httpPost.setConfig(requestConfig);
		// 2.2 设置数据传输格式-json
		httpPost.addHeader("Content-Type", "application/json");
		// 2.3 设置请求实体，封装了请求参数
		StringEntity requestEntity = new StringEntity(JSON.toJSONString(data), "utf-8");
		httpPost.setEntity(requestEntity);

		// 3.发起请求，获取响应信息
		// 3.1 创建httpClient
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;

		try {

			// 3.3 发起请求，获取响应
			response = httpClient.execute(httpPost, new BasicHttpContext());

			if (response.getStatusLine().getStatusCode() != 200) {

				System.out.println(
						"request url failed, http code=" + response.getStatusLine().getStatusCode() + ", url=" + url);
				throw new Exception("请求出错StatusCode：" + response.getStatusLine().getStatusCode());
			}

			// 获取响应内容
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String resultStr = EntityUtils.toString(entity, "utf-8");
				System.out.println("POST请求结果：" + resultStr);

				// 解析响应内容
				JSONObject result = JSON.parseObject(resultStr);

				// 请求失败
				if (result.getInteger("code") == null || 0 != result.getInteger("code")) {

					System.out.println("request url=" + url + ",return value=");
					System.out.println(resultStr);
					int errCode = result.getInteger("code");
					String errMsg = result.getString("message");
					throw new Exception("error code:" + errCode + ", error message:" + errMsg);

					// 请求成功
				} else {
					return result;
				}
			}
		} catch (IOException e) {
			System.out.println("request url=" + url + ", exception, msg=" + e.getMessage());
			e.printStackTrace();
		} finally {
			if (response != null)
				try {
					response.close(); // 释放资源

				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		return null;
	}

	/**
	 * 请求中无文件
	 * 
	 * @param url
	 * @param data
	 * @return
	 * @throws Exception
	 *             CloseableHttpResponse
	 */

	public static CloseableHttpResponse postRequestNoFile(String url, Object paras) {
		// 1.生成一个请求
		HttpPost httpPost = new HttpPost(url);

		// 2.配置请求属性
		// 2.1 设置请求超时时间
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60 * 1000).setConnectTimeout(60 * 1000)
				.build();
		httpPost.setConfig(requestConfig);
		// 2.2 设置数据传输格式-json
		httpPost.addHeader("Content-Type", "application/json");
		// 2.3 设置请求实体，封装了请求参数
		String paraJson = JSON.toJSONString(paras);
		StringEntity requestEntity = new StringEntity(paraJson, "utf-8");
		httpPost.setEntity(requestEntity);

		// 3.发起请求，获取响应信息
		// 3.1 创建httpClient
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;

		// 3.3 发起请求，获取响应
		try {
			response = httpClient.execute(httpPost, new BasicHttpContext());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (response.getStatusLine().getStatusCode() != 200) {

			System.out.println(
					"request url failed, http code=" + response.getStatusLine().getStatusCode() + ", url=" + url);
			throw new RuntimeException("请求出错StatusCode：" + response.getStatusLine().getStatusCode());
		}
		// 有返回值不代表你的逻辑对了，只是post没有报错而已
		return response;
	}

	/**
	 * 请求中有文件
	 * 
	 * @param url
	 * @param data
	 * @param inputStream
	 * @param fileKeyValue
	 * @param fileName
	 * @return
	 * @throws Exception
	 */

	public static CloseableHttpResponse postRequestHasOneFile(String url, Object paras, InputStream inputStream,
			String fileKeyValue, String fileName) {

		HttpPost httpPost = new HttpPost(url);
		CloseableHttpResponse response = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
		httpPost.setConfig(requestConfig);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		// builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		// 第一个参数为 相当于 Form表单提交的file框的name值 第二个参数就是我们要发送的InputStream对象了
		// 第三个参数是文件名
		// 3)
		builder.addBinaryBody(fileKeyValue, inputStream, ContentType.create("multipart/form-data", Consts.UTF_8),
				fileName);
		if (paras instanceof Map) {
			Map<Object, Object> mapPara = (Map) paras;
			for (Entry entry : mapPara.entrySet()) {
				// 构建请求参数 普通表单项
				// 空值不处理
				if (entry.getValue() != null) {
					logger.info("请求参数:{}值为{}", entry.getKey(), entry.getValue());
					StringBody stringCodeBody = new StringBody(entry.getValue().toString(),
							ContentType.MULTIPART_FORM_DATA);
					builder.addPart(entry.getKey().toString(), stringCodeBody);
				} else {
					logger.info("请求参数:{}为空值", entry.getKey().toString());
				}
			}
		}
		HttpEntity entity = builder.build();
		httpPost.setEntity(entity);
		try {
			response = httpClient.execute(httpPost, new BasicHttpContext());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (response.getStatusLine().getStatusCode() != 200) {

			System.out.println(
					"request url failed, http code=" + response.getStatusLine().getStatusCode() + ", url=" + url);
			throw new RuntimeException("请求出错StatusCode：" + response.getStatusLine().getStatusCode());
		}
		// 有返回值不代表你的逻辑对了，只是post没有报错而已
		return response;
	}

	/**
	 * 返回值没有文件
	 * 
	 * @param response
	 * @return
	 */
	public static JSONObject responseNoHasfile(CloseableHttpResponse response) {
		HttpEntity responseEntity = response.getEntity();
		JSONObject result = null;
		if (responseEntity != null) {
			String resultStr = null;
			try {
				resultStr = EntityUtils.toString(responseEntity, "utf-8");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			result = JSON.parseObject(resultStr);
			logger.info(result.toJSONString());
		}
		if (response != null)
			try {
				response.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return result;
	}

	/**
	 * 返回值中有文件
	 * 
	 * @param response
	 * @param fileDir
	 * @param fileName
	 * @return
	 */
	public static File responseHasfile(CloseableHttpResponse response, String fileDir, String fileName) {
		File file = null;
		// 6.取得请求内容
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			// 这里可以得到文件的类型 如image/jpg /zip /tiff 等等
			// 但是发现并不是十分有效，有时明明后缀是.rar但是取到的是null，这点特别说明
			System.out.println(entity.getContentType());
			// 可以判断是否是文件数据流
			System.out.println(entity.isStreaming());
			file = new File(fileDir + fileName);
			// 根据文件路径获取输出流
			try {
				FileOutputStream output = new FileOutputStream(file);
				// 输入流：从钉钉服务器返回的文件流，得到网络资源并写入文件
				InputStream input = entity.getContent();
				// 将数据写入文件：将输入流中的数据写入到输出流
				byte b[] = new byte[1024];
				int j = 0;
				while ((j = input.read(b)) != -1) {
					output.write(b, 0, j);
				}
				output.flush();
				output.close();

				if (entity != null) {
					entity.consumeContent();
				}

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			} finally {
				if (response != null)
					try {
						response.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}

		}

		return file;
	}

	public static InputStream responseHasfileToInputStream(CloseableHttpResponse response) {

		HttpEntity entity = response.getEntity();
		InputStream inputStream = null;
		if (entity != null) {
			// 这里可以得到文件的类型 如image/jpg /zip /tiff 等等
			// 但是发现并不是十分有效，有时明明后缀是.rar但是取到的是null，这点特别说明
			logger.info("ContentType:" + entity.getContentType());
			// 可以判断是否是文件数据流
			if (entity.isStreaming())
				try {
					inputStream = entity.getContent();
				} catch (UnsupportedOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else
				logger.debug("不是文件数据流");

		} else {
			logger.debug("");
		}

		return inputStream;
	}

	/**
	 * 下载文件到本地
	 * 
	 * @param response
	 * @return
	 */
	public static void responseHasfileToLocal(InputStream input, String fileDir, String fileName) {
		FileOutputStream output = null;
		File file = new File(fileDir + File.separator + fileName);
		try {
			output = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte b[] = new byte[1024];
		int j = 0;
		try {
			while ((j = input.read(b)) != -1) {
				output.write(b, 0, j);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
           logger.info("附件下载完成路径："+fileDir + File.separator + fileName);
	}

	
	
	public static CloseableHttpResponse getRequestNoFile(String url, Map<Object, Object> params) {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
		for (Map.Entry<Object, Object> entry : params.entrySet()) {
			if (entry.getValue() != null) {
				pairs.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
			} else {
				logger.info("{} 的值为空", entry.getKey());
			}
		}
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;

		URIBuilder builder = null;
		try {
			builder = new URIBuilder(url);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		builder.setParameters(pairs);
		// 根据地址发送get请求
		HttpGet request = null;
		try {
			request = new HttpGet(builder.build());
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60 * 1000)
					.setConnectTimeout(60 * 1000).build();
			request.setConfig(requestConfig);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 通过请求对象获取响应对象
		try {
			response = httpClient.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (response.getStatusLine().getStatusCode() != 200) {

			System.out.println(
					"request url failed, http code=" + response.getStatusLine().getStatusCode() + ", url=" + url);
			throw new RuntimeException("请求出错StatusCode：" + response.getStatusLine().getStatusCode());
		}
		return response;
	}
      
	
}
