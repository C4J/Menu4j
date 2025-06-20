package com.commander4j.db;

public class JDBFont
{
	private String name = "";
	private String style = "";
	private int size = 10;
	
	public JDBFont(String name,String style,int size)
	{
		this.name = name;
		this.style = style;
		this.size = size;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getStyle()
	{
		return style;
	}
	public void setStyle(String style)
	{
		this.style = style;
	}
	public int getSize()
	{
		return size;
	}
	public void setSize(int size)
	{
		this.size = size;
	}
	
}
