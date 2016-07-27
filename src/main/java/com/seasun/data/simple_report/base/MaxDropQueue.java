package com.seasun.data.simple_report.base;

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MaxDropQueue<E>{
	private static final Log log = LogFactory.getLog(MaxDropQueue.class);
	
	private int capacity = 100;

	public ArrayBlockingQueue<E> queue;

	public MaxDropQueue(int capacity) {
		super();
		this.capacity = capacity;
		queue = new ArrayBlockingQueue<>(capacity);
	}
	
	public int getCapacity(){
		return capacity;
	}
	
	public int size(){
		return queue.size();
	}

	
	//阻塞式写，当队列满时，移除最老的消息
	public void put(E e) {
		while(queue.size() >= capacity){
			try {
				E dopE = queue.take();
				log.debug("			queue is full, have to drop: " + dopE);
				break;
			} catch (InterruptedException e1) {
				log.info(e1);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e2) {
					log.info(e2);
				}
			}
		}
		while(true){
			try {
				queue.put(e);
				break;
			} catch (InterruptedException e1) {
				log.error(e1);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e2) {
					log.error(e2);
				}
			}
			
		}
		
	}
	
	//阻塞式读
	public E take(){
		while(true){
			try {
				return queue.take();
			} catch (InterruptedException e) {
				log.error(e);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e2) {
					log.error(e2);
				}
			}
		}
		
	}

}
