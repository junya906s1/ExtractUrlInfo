package com.laonbud.feeltering.extract.urlinfo;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author s1
 *	extract the text and image(depend on the text).
 *	2016_04_26
 */


public class ExtractTextnImage {
	private Elements divs = new Elements();
	private int longestNode;
	
	private String extractedTEXT = new String();
	private String extractedIMAGE = new String();
	
	private Element IMAGEfromSiblings = null;
	private Element IMAGEfromChildren = null;

	// extract the Text & Image
	public String extractTEXT(Document _doc){
		divs = _doc.getElementsContainingOwnText(" ");
		longestNode = 0;
		
		for(Element div : divs){
			if(div.hasText()){
				if((div.text().length() + div.siblingElements().text().length()) > longestNode){
					longestNode = div.text().length() + div.siblingElements().text().length();
					
					// extract text
					extractedTEXT = div.parent().children().text();
					
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// extract img elements of siblings
					for(Element img : div.siblingElements().select("img")){									// 본문을 포함한 div의 형제노드에서 img가 있는 것을 찾음
						if((img.attr("width") != "") && (img.attr("width").contains("%") != true)){			// img tag의 width 값이 없거나 %가 들어간 것 제외 (세번째 방식에서 처리할 것)
							String temp = img.attr("width").replace("px", "");								// img tag의 width 값에 px가 있으면 없앰
							
							if(Integer.parseInt(temp) >= 200){												// width가 200이상인 img를 IMAGEfromSiblings에 저장 
								IMAGEfromSiblings = img.select("img").first();								// (밑에 extractIMAGE(Document _doc)에서 사용할 것)
								break;
							}else if(Integer.parseInt(temp) >= 150){										// width가 200이상이 없을 때 150 이상인 것을 찾고 img를 IMAGEfromSiblings에 저장
								IMAGEfromSiblings = img.select("img").first();
								break;
							}
						}
						// %를 포함한 것 우선 그냥 제외 (3번째 방법에서 찾을 수도 있으니)
						//else if((img.attr("width") != "") && (img.attr("width").contains("%") == true)){
						//	IMAGEfromSiblings = img.select("img");
					}
					// extract img elements of children
					for(Element img : div.children().select("img")){
						if((img.attr("width") != "") && (img.attr("width").contains("%") != true)){
							String temp = img.attr("width").replace("px", "");
							
							if(Integer.parseInt(temp) >= 200){												// width가 200이상인 img를 IMAGEfromChildre에 저장 
								IMAGEfromChildren = img.select("img").first();								// (밑에 extractIMAGE(Document _doc)에서 사용할 것)
								break;
							}else if(Integer.parseInt(temp) >= 150){										// width가 200이상이 없을 때 150 이상인 것을 찾고 img를 IMAGEfromChildre에 저장
								IMAGEfromChildren = img.select("img").first();
								break;
							}
						}
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				}
			}
		}
		return extractedTEXT;
	}
	
	// if there is no image in meta data, call the Image
	// have to use after extractTEXT()!!!!!!
	public String extractIMAGE(Document _doc){
		if(IMAGEfromSiblings != null){
			extractedIMAGE = IMAGEfromSiblings.attr("abs:src");
			//System.out.println("image from sibling node");
			return extractedIMAGE;
		}else if(IMAGEfromSiblings == null && IMAGEfromChildren != null){
			extractedIMAGE = IMAGEfromChildren.attr("abs:src");
			//System.out.println("image from chidren node");
			return extractedIMAGE;
		}
		return null;
	}
}
