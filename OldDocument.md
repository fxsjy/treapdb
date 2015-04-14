# TreapDB 1.x Document #

## Features ##
  * random "set" speed: 8000 tps when key's amount is less than 10,000,000.

  * random "get" speed: 15000 tps when key's amount is less than 10,000,000.

  * support other operations more than "get" and "set": "kmin", "kmax", "prefix", "len","range".(because it is not hashmap but a balanced search tree.)
## Benchmark ##

> http://code.google.com/p/treapdb/wiki/SomeBenchmark

## RPC Server ##

  * memcached protocol compatible server for **test usage**
  * thrift-based server for production usage

## Download & Configure ##
  * click here to download http://code.google.com/p/treapdb/downloads/list

  * run
    * ./treapdb\_thrift.sh (server based on thrift protocol)
    * ./treapdb\_mc.sh (server based on memcached protocol)

  * configuration:


> modify the scripts above , and change this line:

```
     java $JAVA_OPTS -cp $LIBS fx.sunjoy.FastTreapDB 11811 "./data/dbhere" 64 128

```

  * parameters:
    * 11811 is the listening port;
    * "./data/dbhere" is the index file path;
    * 64(B) is the index-block-item size (keep it, if your key is not longer that 30 bytes)
    * 128(MB) is the memory-map-size (the more the better, if physical memory is big enough. Disk seeking will be more frequent, when index file size is bigger than memory-map-size parameter.)


  * Case Study: when memory-map-size is set to 6.4 G, a client insert 0.1 billion key-value pairs（key:30 bytes, value:255 bytes） to treapdb server with stable speed at 7200(insertion/second). After insertion, the query operation can also response in less than 10 ms.

## How to build from source ##

  1. svn checkout http://treapdb.googlecode.com/svn/trunk/
  1. ant dist
  1. use the jars generated in build/dist

## How to write client code ##

```
     String host = "localhost";
     Integer port = 11811;
     TreapDBClient client = TreapDBClientFactory.getClient(host, port);
     client.put(...)
     client.get(...)
     client.prefix(...)
     ...
```
## Can TreapDB be used in an embedded way? ##

> Yes,put libtreap-xx.jar in your app's classpath.
```
    DiskTreap<String, Serializable> treap = new DiskTreap<String,Serializable>(64,new File("/usr/local/indexpath"),67108864); 
//64 is the size of index block, 67108864 is the size of memory map  be used.
    treap.put(...)
    treap.get(...)
    ...

    PS. key can be any type which implements comparable; value can be any type which implements Serializable
```

## Only Java? ##

  * TreapDB server is based on thrift, so you can generate your client in any programming language.

  * For example, generate the python client code.
> > thrift --gen py res/service.txt
  * res/service.txt is a service specification:
```
       namespace java fx.sunjoy.server.gen

       service	TreapDBService{
	   void put(1:string key, 2:binary value);
	   binary get(1:string key),
	   map<string,binary> prefix(1:string prefixStr,2:i32 limit),
	   map<string,binary> kmax(1:i32 k),
	   map<string,binary> kmin(1:i32 k),
	   map<string,binary> range(1:string kStart, 2:string kEnd,3:i32 limit),
	   i32 length(),
	   bool remove(1:string key)
       } 


```


> Python client: http://code.google.com/p/treapdb/wiki/PythonClientExample
## What is treap? ##

  * In computer science, the treap and the randomized binary search tree are two closely-related forms of binary search tree data structures that maintain a dynamic set of ordered keys and allow binary searches among the keys. After any sequence of insertions and deletions of keys, the shape of the tree is a random variable with the same probability distribution as a random binary tree; in particular, with high probability its height is proportional to the logarithm of the number of keys, so that each search, insertion, or deletion operation takes logarithmic time to perform.

http://en.wikipedia.org/wiki/Treap

## Contact the author ##
  * Junyi Sun, Qiang Ma
  * E-mail: ccnusjy (#$) gmail.com
  * Sponsor: [Sino-German Joint Laboratory of Software Integration](http://sigsit.ict.ac.cn/)