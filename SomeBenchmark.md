# Embedded test case #

  * Integer key

```

                DiskTreap<Integer, Integer> treap = new DiskTreap<Integer,Integer>(64,new File(path),128<<20);
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			key = i;
			Integer value = i;
			treap.put(key, value);
			if(i%100==0){
				System.out.println("inserting: "+i);
			}
		}
		System.out.println("put cost:"+ (System.currentTimeMillis()-t1)+" ms");
```

1,000,000 random keys, 44000 per second.

  * String Key
```
                DiskTreap<String, Serializable> treap = new DiskTreap<String,Serializable>(64,new File(path),64<<20);
		ByteBuffer buf = ByteBuffer.allocate(100);
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			treap.put("thing"+String.format("%010d", key),buf.array());
			//String v = treap.get("thing"+i);
			if(i%100==0){
				System.out.println("inserting:"+i);
			}
		}

```
1,000,000 random keys, 15600 per second


# RPC test case #

```

                TreapDBClient client = TreapDBClientFactory.getClient(host, 11811);
		
		
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			
			client.put("thing"+String.format("%010d", key),buf);
			
			if(i%100==0)
				System.out.println("inserting:"+i);
		}
		System.out.println(System.currentTimeMillis()-t1);
```

1,000,000 random keys, 7546 per second