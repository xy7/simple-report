package com.seasun.data.simple_report.base;

import static org.junit.Assert.*;

import org.junit.Test;

public class EventTypeTest {

	@Test
	public void test() {
		for(EventType e:EventType.values()){
			System.out.println(e);
			System.out.println(e.name());
			System.out.println(e.ordinal());
			System.out.println(e.getValue());
			System.out.println("-------");
		}
		
		EventType e = EventType.BLINK_STRENGTH;
		switch(e){
		case BLINK_STRENGTH:
			break;
		case POOR_SIGNAL_LEVEL:
			;
			break;
		}
		
		//EventType ev = new EventType("esense");
	}

}
