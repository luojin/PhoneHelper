package com.app.androidsms.util;

public class NameNumberPair {
	String name;
	String number;
	
	public NameNumberPair()
	{
		name=number=null;
	}
	
	public NameNumberPair(String name, String number)
	{
		this.name = name;
		this.number = number;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNumber() {
		return number==null?"":number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
}
