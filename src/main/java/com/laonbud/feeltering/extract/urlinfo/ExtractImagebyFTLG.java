package com.laonbud.feeltering.extract.urlinfo;

import java.awt.Dimension;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.xml.sax.SAXException;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.Image;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.sax.ImageExtractor;

/**
 * @author s1
 *	extract image by reading width & height in header of image url
 *	2016_04_26
 *	future work
 *	- 문서내의 모든 img url을 불러오는 부분 수정
 */


public class ExtractImagebyFTLG {
	
	public String extractIMAGE2nd(String _url) throws MalformedURLException{
		List<ImageInfo> imageInfoList = new ArrayList<ImageInfo>();
		try{
			List<Image> imgUrls = ImageExtractor.INSTANCE.process(new URL(_url), CommonExtractors.ARTICLE_EXTRACTOR);
			// 이부분 수정
			for(Image imgUrl : imgUrls){
				ImageInfo temp = null;
				if(!imgUrl.getSrc().startsWith("http")){	// convertUrlRelativeToAbsoulte(), 상대주소 절대주소 처리
					temp = new ImageInfo(convertUrlRelativeToAbsoulte(_url, imgUrl.getSrc()));
					imageInfoList.add(temp);
				}else{
					temp = new ImageInfo(imgUrl.getSrc());
					imageInfoList.add(temp);
				}
			}// 여기까지
		}catch(IOException e){	System.out.println("error IOException");	
		}catch(BoilerpipeProcessingException e){	System.out.println("error BoilerpipeProcessingException");
		}catch(SAXException e){	System.out.println("error SAXException");
		}

		updateImageDimension(imageInfoList);

		//System.out.println(selectBestImage(imageInfoList));
		return selectBestImage(imageInfoList);
	}
	
	// image info
	public class ImageInfo {
		private String url;
		private int width;
		private int height;

		public ImageInfo(String url) {
			this.url = url;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public int getArea() {
			if (height <= 0 || width <= 0)
				return -1;
			return width * height;
		}

		public float getRatio() {
			if (height <= 0 || width <= 0)
				return -1;
			return ((float) width) / height;
		}

		@Override
		public String toString() {
			return "ImageInfo [url=" + url + ", width=" + width + ", height=" + height + "]";
		}
	}
	
	// 대상이 되는 전체 이미지 리스트에 대해 thread pool을 만들어 제한된 갯수만큼 동시에 이미지 dimension 조회
	private void updateImageDimension(List<ImageInfo> imageInfoList) {
		// executor가 thread pool, 최대 IMAGE_DOWNLOAD_POOL_COUNT 만큼 THREAD를 가지고
		// thread scheduling
		int IMAGE_DOWNLOAD_POOL_COUNT = 10;
		final ExecutorService executor = Executors.newScheduledThreadPool(IMAGE_DOWNLOAD_POOL_COUNT);
		List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();

		for (int i = 0; i < imageInfoList.size(); i++) {
			Future<Integer> future = executor.submit(new ImageDimensionResolveTask(imageInfoList.get(i)));
			futureList.add(future);
		}
		executor.shutdown(); // This does not cancel the already-scheduled task.

		// 추가 작업을 받지 않고 최대 1초간 전체 작업 종료 대기
		try {
			if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
				// 5초동안 전체 작업이 종료되지 않은 경우
				System.out.println("아직 처리중인 작업 존재");
				System.out.println("작업 강제 종료 실행");
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
		}

		System.out.println("awaitTermination 통과");
		
		// future .isDone, future.cancle 추가
		for (Future<Integer> future : futureList) {
			try {
				if(future.isDone()){
					future.get();
				}//else future.cancel(true);
			} catch (Exception e) {

			}
		}
	}
	
	// thread pool에서 작업하게 되는 실제 thread task
	private class ImageDimensionResolveTask implements Callable<Integer> {
		private ImageInfo imageInfo;

		public ImageDimensionResolveTask(ImageInfo imageInfo) {
			this.imageInfo = imageInfo;
		}

		//@Override
		public Integer call() throws Exception {
			Dimension dimension = readUrlImageDimension(imageInfo.getUrl());
			if(dimension != null){
				imageInfo.setWidth((int) dimension.getWidth());
				imageInfo.setHeight((int) dimension.getHeight());
			}
			return null;
		}
	}

	// image 주소에 대해 connection을 맺고 실제 image body data를 조회하는 대신
	// header쪽의 image width, height만 확인, 실제 image body를 읽어오지 않기 때문에 빠르게 return
	public static Dimension readUrlImageDimension(String src) {
		URL url = null;
		try {
			url = new URL(src);
		} catch (MalformedURLException e) {
			return null;
		}
		return readUrlImageDimension(url);
	}

	public static Dimension readUrlImageDimension(URL url) {
		try {
			InputStream inputStream = url.openStream();

			try (ImageInputStream in = ImageIO.createImageInputStream(inputStream)) {
				final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
				if (readers.hasNext()) {
					ImageReader reader = readers.next();
					try {
						reader.setInput(in);
						return new Dimension(reader.getWidth(0), reader.getHeight(0));
					} finally {
						reader.dispose();
					}
				}
			}
		} catch (Exception e) {
		}

		return new Dimension(0, 0);
	}

	// width, height가 update된 imageInfoList에 대해 가장 적합한 이미지 추출
	private String selectBestImage(List<ImageInfo> imageInfoList) {

		for (ImageInfo imageInfo : imageInfoList) {
			//System.out.println(imageInfo.toString());					// check
			if (imageInfo.getArea() <= 10000)
				continue;
			if (imageInfo.getRatio() > 3.0)
				continue;
			if (imageInfo.getRatio() < 1.0 / 3.0)
				continue;

			if (imageInfo.getArea() > 40000)
				return imageInfo.getUrl();
		}
		return null;
	}
	
	public static String convertUrlRelativeToAbsoulte(String base, String relative) {
		if( (base==null || base.length()<1) || (relative==null || relative.length()<1)) return null;
				
		URL url = null;
		try { url = new URL(base); } 
		catch (MalformedURLException e) { 
			e.printStackTrace();
			return null;
		}
		
		
		if(relative.startsWith("//")) {
			return url.getProtocol() + ":" + relative;
		}
		
		String host = url.getProtocol() + "://" + url.getHost() + ((url.getPort() >= 0)?":"+url.getPort():"");
		String path = url.getPath();
		
		Stack<String> stack = new Stack<String>();
		// path는 항상 '/'로 시작, '/' 기준으로 왼쪽의 아이템을 취함.
		String[] pathItems = path.split("\\/");
		int i = 1;
		for(; i < pathItems.length; i++) {
			stack.push(pathItems[i]);
		}
		// 특정 파일등이 경로끝에 있는 경우 (/index.html), 제거
		if(!path.endsWith("/") && stack.size() > 0) stack.pop();
		
		String[] parts = relative.split("\\/");
		
		i = 0;
		// '/'로 시작하는 절대 경로의 경우 모든 path item을 pop
		if(relative.startsWith("/")) {
			while(stack.size() > 0) {
				stack.pop();
			}
			// '/'로 시작할 경우 첫 아이템이 '/' 좌측의 빈문자열이기 때문에 skip
			i++;
		}
		
		for(; i < parts.length; i++) {
			if(parts[i].equals(".")) continue;
			// path상 parent로 한단계 올라감
			if(parts[i].equals("..")) {
				if(stack.size() > 0) stack.pop();
			}
			else stack.push(parts[i]);
		}
		
		StringBuilder sb = new StringBuilder();		
		Iterator<String> stackItr = stack.iterator();
		while(stackItr.hasNext()) {
			sb.append("/");
			sb.append(stackItr.next());			
		}
		//sb.setLength(sb.length()-1);
		
		return host + sb.toString();
	}
	
}
