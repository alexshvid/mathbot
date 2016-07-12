package com.shvid.mathbot;

import org.junit.Assert;
import org.junit.Test;

public class QueryServiceTest {

	private final QueryService queryService = new  QueryService();
	
	@Test
	public void test() {
		
		String query = "sqrt(445)*system(\"whoami\")";
		
		Assert.assertFalse(queryService.isValidQuery(query));
		
	}
	
}
