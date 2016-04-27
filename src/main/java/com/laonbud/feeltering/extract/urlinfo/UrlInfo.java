package com.laonbud.feeltering.extract.urlinfo;

/**
 * @author s1
 *	URL information
 *	URL, Title, Description, Image, Text
 *	2016_04_26
 */

public class UrlInfo {
	private String tURL;
	private String Title;
	private String Description;
	private String Image;
	private String Text;
	
	public void setURL(String _url){
		this.tURL = _url;
	}
	public void setTitle(String _title){
		this.Title = _title;
	}
	public void setDescription(String _description){
		this.Description = _description;
	}
	public void setImage(String _image){
		this.Image = _image;
	}
	public void setText(String _text){
		this.Text = _text;
	}
	public void showObjt(){
		System.out.println("URL : " + tURL);
		System.out.println("Title : " + Title);
		System.out.println("Description : " + Description);
		System.out.println("Image : " + Image);
		System.out.println("Text : " + Text);
	}
	public String getURL(){
		return tURL;
	}
	public String getTitle(){
		return Title;
	}
	public String getDescription(){
		return Description;
	}
	public String getImage(){
		return Image;
	}
	public String getText(){
		return Text;
	}
}
