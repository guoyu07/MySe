package com.gs.action;

import java.util.LinkedList;

import com.gs.searcher.Hit;
import com.gs.searcher.Searcher;
import com.opensymphony.xwork2.ActionSupport;

public class SearcherAction extends ActionSupport {

	private static final long serialVersionUID = -5206663242253958019L; 
	private LinkedList<Hit> list;
	public String search(){
		Searcher s = new Searcher("D://Test//json");
		list = s.search("中国");
		System.out.println(list);
		return SUCCESS;
	}
	/**
	 * @return the list
	 */
	public LinkedList<Hit> getList() {
		return list;
	}
	/**
	 * @param list the list to set
	 */
	public void setList(LinkedList<Hit> list) {
		this.list = list;
	}
	
}
