package com.laonbud.feeltering.extract.urlinfo;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author s1
 *	title, description, image, text extract
 *	return UrlInfo object
 *	2016_04_27
 */

public class ExtractUrlInfo {

	public UrlInfo ExtractInfo(String _targetURL) throws IOException{
		// URL 양 끝 공백제거
		String targetURL = _targetURL.trim();
		
		// urlinfo 객체 생성
		UrlInfo resultURLInfo = new UrlInfo();
		
		try{		
			// URL 연결 (jsoup)
			Document doc = Jsoup.connect(targetURL)
					.userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:39.0) Gecko/20100101 Firefox/39.0")
					.ignoreContentType(true)
		    		.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,**;q=0.8")
		    		.header("Accept-Encoding", "gzip, deflate")
		    		.timeout(10*1000)
		    		//.ignoreHttpErrors(true)
		    		//.followRedirects(true)
					.get();

			// if input URL is image/~, just return input image
			String urlType = new URL(targetURL).openConnection().getContentType();
			if(urlType != null){
				if(urlType.startsWith("image/")){
					// UrlInfo에 이미지 부분에 바로 저장
				}
			}
			
			resultURLInfo.setURL(targetURL);
			
			// meta data로 image, title, description 추출
			ExtractFromMeta meta = new ExtractFromMeta();
			
			resultURLInfo.setTitle(meta.extractTitle(doc));
			resultURLInfo.setDescription(meta.extractDescription(doc));
			resultURLInfo.setImage(meta.extractImageUrl(doc));
			System.out.println("a : " + meta.extractImageUrl(doc));

			
			// 2nd : 본문, image 추출
			ExtractTextnImage TxtnImg = new ExtractTextnImage();
			
			resultURLInfo.setText(TxtnImg.extractTEXT(doc));
			if(resultURLInfo.getImage() == null)
				resultURLInfo.setImage(TxtnImg.extractIMAGE(doc));
			System.out.println("b : " + TxtnImg.extractIMAGE(doc));
			
			// 3nd : image 추출
			ExtractImagebyFTLG imgFT = new ExtractImagebyFTLG();
			
			if(resultURLInfo.getImage() == null)
				resultURLInfo.setImage(imgFT.extractIMAGE2nd(targetURL));
			System.out.println("c : " + imgFT.extractIMAGE2nd(targetURL));

			
		}catch(Exception e){
			e.printStackTrace();
		}

		// 최종결과 (urlinfo 객체 리턴)
		return resultURLInfo;
	}
}


