package com.commander4j.process;

public class JEnvironmentVariable {

	public String key = "";
	public String variable = "";
	
	public JEnvironmentVariable(String k,String v)
	{
		this.key = k;
		this.variable = v;
	}
	
	public String toString()
	{
		return key+"="+variable;
	}
}
