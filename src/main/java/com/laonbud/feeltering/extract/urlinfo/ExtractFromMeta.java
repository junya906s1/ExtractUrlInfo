package com.laonbud.feeltering.extract.urlinfo;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Given a url to a web page, extract a suitable image from that page. This will
 * attempt to follow a method similar to Google+, as described <a href=
 * "http://webmasters.stackexchange.com/questions/25581/how-does-google-plus-select-an-image-from-a-shared-link"
 * >here</a>
 * 
 */

/**
 * @author s1
 *	Extract image, title, description from meta data
 *	2016_04_26
 */

public class ExtractFromMeta {
	
	public String extractImageUrl(Document _doc) {
		
		String extractedURL = null;
		
		// 1
		extractedURL = getImageFromSchema(_doc);
        if (extractedURL != null) {
            return extractedURL;
        }

        // 2
        extractedURL = getImageFromOpenGraph(_doc);
        if (extractedURL != null) {
            return extractedURL;
        }

        // 5
        extractedURL = getImageFromLinkRel(_doc);
        if (extractedURL != null) {
            return extractedURL;
        }
		
		return extractedURL;
	}
	
	// 1
    private String getImageFromSchema(Document document) {
        Element container = document.select("*[itemscope][itemtype=http://schema.org/ImageObject]").first();
        if (container == null) {
            return null;
        }

        Element image = container.select("img[itemprop=contentUrl]").first();
        if (image == null) {
            return null;
        }
        return image.absUrl("src");
    }
    
    // 2 using meta tag image
    private String getImageFromOpenGraph(Document document) {
        Element image = document.select("meta[property=og:image]").first();
        if (image != null) {
            return image.attr("abs:content");
        }
        // twitter
        image = document.select("meta[name=twitter:image]").first();
        if (image != null) {
            return image.attr("abs:content");
        }
        
        Element secureImage = document.select("meta[property=og:image:secure]").first();
        if (secureImage != null) {
            return secureImage.attr("abs:content");
        }
        // twitter
        secureImage = document.select("meta[name=twitter:image:secure]").first();
        if (secureImage != null) {
            return secureImage.attr("abs:content");
        }
        return null;
    }
    
    
	// 5 using link tag and rel property
    private String getImageFromLinkRel(Document document) {
        Element link = document.select("link[rel=image_src]").first();
        if (link != null) {
            return link.attr("abs:href");
        }
        return null;
    }
    
    
    
    
    // extract title from meta data
    public String extractTitle(Document _doc){
    	
    	String extractedTITLE = null;
    	extractedTITLE = getTitleFromMetadata(_doc);
    	
    	return extractedTITLE;
    }
    
	// extract title from meta data
	private String getTitleFromMetadata(Document document){
		Element title = document.select("meta[property=og:title]").first();
		if(title != null){
			return title.attr("content");
		}
		// 트위터 태그
		title = document.select("meta[name=twitter:title]").first();
		if(title != null){
			return title.attr("content");
		}
		
		Element titleTag = document.select("title").first();
		if(titleTag != null){
			return titleTag.text();
		}
		return null;
	}

	
	// extract description from meta data	
    public String extractDescription(Document _doc){
    	
    	String extractedDESCRIPTION = null;
    	extractedDESCRIPTION = getDescrptFromMetadata(_doc);
    	
    	return extractedDESCRIPTION;
    }
    
	// extract description from meta data
	private String getDescrptFromMetadata(Document document){
		Element description = document.select("meta[property=og:description]").first();
		if(description != null){
			return description.attr("content");
		}
		// 트위터 태그
		description = document.select("meta[name=twitter:description]").first();
		if(description != null){
			return description.attr("content");
		}
		return null;
	}
}
