package com.seasun.data.simple_report.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.seasun.data.simple_report.SimpleReportApp;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SimpleReportApp.class)
public class dataControllerTest {

	@Autowired
	public DataController dc;
	
	@Test
	public void getEsenceDataTest() {
		System.out.println(dc.getEsenceData("2016-05-12 00:00:00", "2016-05-14 00:00:00"));
	}

}
