/**
 * GS
 */
package com.gs.searcher;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.gs.crawler.PagePOJO;


/**
 * 读取分布式爬虫处理好的Json文件
 * @author GaoShen
 * @packageName com.gs.io
 */
public class JsonReader implements Closeable{
	private static Logger logger = Logger.getLogger(JsonReader.class);
	private FileInputStream fis;
	private long flag = 0;
	private File file;
	/**
	 * 用commons IO包里面的ReadLines方法实现，当问价过大的时候内存会溢出�?
	 * 将这个文件里的所有json格式的内容制成PagePOJO格式，然后封装在LinkedList中返回�? 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static LinkedList<PagePOJO> readFileToPOJOs(String path) throws IOException {
		List<String> list = null;
		try {
			list = FileUtils.readLines(new File(path));
		} catch (OutOfMemoryError e) {
			logger.fatal(e.getMessage());
		}
		Gson g = new Gson();
		LinkedList<PagePOJO> re = new LinkedList<PagePOJO>();
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			re.add(g.fromJson(it.next(), PagePOJO.class));
		}
		return re;
	}
	
	/**
	 * 读出指定偏移量的�?��PagePOJO
	 * @param file 
	 * @param startoffset
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static PagePOJO read(File file, long startoffset) throws FileNotFoundException,IOException {
		String json = null;
		FileInputStream fis = new FileInputStream(file);
		try {
			fis.skip(startoffset);
			byte[] b1 = new byte[99999];// Buffer
			byte b;
			int i=0;
			while(fis.read() != '{'){
				
			}
			for (i = 0; (b = (byte) fis.read()) != -1 && b != '}'; i++) {
				//if(i==0 && b != '{'){i = i -1;continue;}
				b1[i] = b;
			}
			 byte[] b2 = new byte[i];
             int j = 0;
             for (j = 0; j < i; j++) {
                     b2[j] = b1[j];// 抹掉b1后边�?
             }
			json = '{'+new String(b2)+'}';
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		Gson gson = new Gson();
		fis.close();
		try {
			return gson.fromJson(json, PagePOJO.class);
		} catch (JsonSyntaxException e) {
			logger.warn(json+"格式错误");
			return null;
		}
	}
	
	/**
	 * 从FileinputStream指定好的位置读取�?��PagePOJO
	 * @param fis
	 * @return
	 * @throws IOException 
	 */
	public static PagePOJO read(FileInputStream fis){
		String json = null;
		byte[] b1 = new byte[99999];// Buffer
		byte b;
		try {
			for (int i = 0; (b = (byte) fis.read()) != -1 && b != '}'; i++) {
				b1[i] = b;
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		json = new String(b1)+'}';
		Gson gson = new Gson();
		PagePOJO result = null;
		try {
			result = gson.fromJson(json, PagePOJO.class);
		} catch (JsonSyntaxException e) {
			logger.warn(json+"格式错误");
		}
		return result;
	}
	
	/**
	 * 如果用到next方法必须调用此构造函数来初始化File
	 * @param file
	 * @throws FileNotFoundException 
	 */
	public JsonReader(final File file) throws FileNotFoundException{
		this.fis = new FileInputStream(file);
		this.file = file;
	}
	/**
	 * 读取下一个Json，前提是已经初始化了FileinputStream
	 * @return
	 * @throws Exception 
	 */
	public Hit next() throws Exception{
		if(fis == null) throw new Exception("FileInputStream未初始化，调用JsonReader(File)来初始化FileinputStream");
		  Hit hit = new Hit();
          String json = null;
          int i = 0;
          try {
                  byte[] b1 = new byte[99999];// Buffer
                  byte b = 0;
                  if (flag == 0) {//补上因为判断文件是否到头而错过的�?
                          b1[0] = '{';
                          i=1;
                  }else{
                          i=0;
                  }
                  for (; b != '}'&& b != -1; i++) {
                          b = (byte) fis.read();
                          if(b == '\n') {i = i-1;continue;} //第二次开始每次都有一个换行符，丢弃�?
                          b1[i] = b;
                  }
                  byte[] b2 = new byte[i];
                  int j = 0;
                  for (j = 0; j < i; j++) {
                          b2[j] = b1[j];// 抹掉b1后边�?
                  }
                  json = new String(b2);
          } catch (IOException e) {
        	  logger.error(e.getMessage());
          }
          Gson gson = new Gson();
          PagePOJO pojo;
		try {
			pojo = gson.fromJson(json, PagePOJO.class);
		} catch (JsonSyntaxException e) {
			flag = flag + i;
			logger.warn(json+"格式错误");
			return null;
		}
          hit.setPagePOJO(pojo);
          if (flag != 0)
                  hit.setStartOffset(flag + 1);
          else
                  hit.setStartOffset(flag);// 第一行的起始偏移量是0
          hit.setFileName(this.file.getName());
          flag = flag + i;
          return hit;
	}

	/**
	 * 调用next方法之前�?��查询是否还有下一个Json内容
	 * @return
	 * @throws IOException
	 */
	public boolean hasNext() throws IOException {
		if (fis.read() == -1)
			return false;
		else{
			return true;
		}
	}
	
	public void close() throws IOException{
		try {
			this.fis.close();
		} catch (Exception e) {
			logger.error("fis关闭失败"+e.getMessage());
		}
	}

}
