package com.seasun.data.simple_report.base;

import java.time.LocalDateTime;

import org.junit.Test;

public class MaxDropQueueTest {

	//junit在死循环一段时间后会中止执行
	@Test
	public void test() {
		MaxDropQueue<LocalDateTime> mdq = new MaxDropQueue<>(5);
		
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while(true){
					System.out.println("take:" + mdq.take());
					try {
						Thread.sleep((int) (Math.random() * 3000));
					} catch (InterruptedException e) {
					}
				}

			}
		});
		t.start();
		
		for (int i = 0; i < 10; i++) {

			final int thisI = i;
			Thread t2 = new Thread(new Runnable() {

				@Override
				public void run() {
					while(true) {
						int r = thisI * 100 + (int)(Math.random()*100);
						//System.out.println("put beg: " + r + " size: " + mdq.size());
						mdq.put(LocalDateTime.now().plusNanos(r));
						//System.out.println("put end: " + r + " size: " + mdq.size());
						try {
							Thread.sleep((int) (Math.random() * 1000));
						} catch (InterruptedException e) {
						}

					}

				}
			});
			t2.start();

			try {
				Thread.sleep((int) (Math.random() * 3000));
			} catch (InterruptedException e) {
			}

		}

	}

}
