package fx.sunjoy.test;

import java.io.File;

import fx.sunjoy.algo.impl.DiskTreap;

public class ConcurrentTest {
	private DiskTreap<Integer, Integer> diskTreap = null;
	
	public ConcurrentTest() throws Exception{
		diskTreap = new DiskTreap<Integer, Integer>(new File("c:/test/db/concurrent2"));
	}
	
	public void write(){
		for(int i=0;i<100000;i++){
			Integer key = i;
			Integer value = i+1;
			diskTreap.put(key, value);
			if(i%100==0){
				System.out.println(Thread.currentThread().getId()+">  inserting: "+i);
			}
		}
	}
	
	public void read(){
		for(int i=0;i<100000;i++){
			Integer key = i;
			if(i%100==0){
				System.out.println(Thread.currentThread().getId()+"> getting: "+i+":"+diskTreap.get(key));
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception{
		final ConcurrentTest  test = new ConcurrentTest();
		Runtime.getRuntime().addShutdownHook(new Thread() {
		      public void run() {
		        System.out.println("Running Shutdown Hook");
		        test.destory();
		      }
	    });

		for(int i=0;i<5;i++){
			new Thread(
					new Runnable(){
						public void run(){
							test.write();
						}
					}
			).start();
		}

		for(int i=0;i<150;i++){
			new Thread(
					new Runnable(){
						public void run(){
							test.read();
						}
					}
			).start();
		}
		Thread.sleep(60*1000);
		System.out.println("===========================");
		
	}

	
	private void destory() {
		this.diskTreap.close();
	}
}
