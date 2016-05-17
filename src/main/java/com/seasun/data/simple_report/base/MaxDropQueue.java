package com.seasun.data.simple_report.base;

import java.util.concurrent.ArrayBlockingQueue;

public class MaxDropQueue<E>{
	
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
				//System.out.println("				drop: " + dopE);
				break;
			} catch (InterruptedException e1) {
				System.out.println(e1);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e2) {
					System.out.println(e2);
				}
			}
		}
		while(true){
			try {
				queue.put(e);
				break;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e2) {
					System.out.println(e2);
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
				System.out.println(e);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e2) {
					System.out.println(e2);
				}
			}
		}
		
	}

}
