package com.natsub.ws;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.natsub.ws.Topic;

public class TopicTest {
	
	@Test
	public void splitString() {
		String str = "a,b,c";
		String[] strs = Topic.splitString(str);
		assertArrayEquals(new String[]{"a", "b", "c"}, strs);
		
		str = " a, b , c";
		strs = Topic.splitString(str);
		assertArrayEquals(new String[]{"a", "b", "c"}, strs);
	}
}
