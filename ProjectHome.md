# TreapDB is a  key-value store based on Treap #




## News ##
  * 2.0 RC1 released!
  * See the change log. http://code.google.com/p/treapdb/wiki/ChangeLog
  * [中文简介](http://code.google.com/p/treapdb/wiki/XiangMuTeDian)

## Unit Test ##
  * [See the test case to learn how to use TreapDB](http://code.google.com/p/treapdb/source/browse/trunk/src/fx/sunjoy/test/unittest/BaseTreapDBTest.java)

## Features ##
  * can be  a embedded library or a standalone RPC-Server

  * can 'talk' memcache and thrift at the same time

  * master-slave replication

  * random "put" speed: 8000 tps(RPC Server); 25000tps(Embedded)
    * key is "thing1"~"thing5000000",value is 100B string
    * 关于性能http://code.google.com/p/treapdb/issues/detail?id=6

  * random "get" speed: 15000 tps(RPC Server); 32000tps(Embedded)

  * bulkPut speed: **17000** tps, bulkGet speed:30000tps

  * support other operations besides "get" and "set":
> > [All Operations](http://code.google.com/p/treapdb/wiki/Operations)


  * service specification:
```
namespace java fx.sunjoy.server.gen

struct Pair {
  1: string key,
  2: binary value,
}

service	TreapDBService{
	void put(1:string key, 2:binary value);
	binary get(1:string key),
	void bulkPut(1:map<string,binary> kvMap);
	list<Pair> bulkGet(1:list<string> keyList);
	list<Pair> prefix(1:string prefixStr,2:i32 limit,3:string startK,4:bool asc),
	list<Pair> bulkPrefix(1:list<string> prefixList,2:i32 limit,3:string startK,4:bool asc),
	list<Pair> kmax(1:i32 k),
	list<Pair> kmin(1:i32 k),
	list<Pair> range(1:string kStart, 2:string kEnd,3:i32 limit),
	list<Pair> before(1:string key,2:i32 limit),
	list<Pair> after(1:string key,2:i32 limit),
	i32 length(),
	bool remove(1:string key),
	bool removePrefix(1:string key),
	void optimize(1:i32 amount)
} 


```

# Operations of TreapDB #


|**Operation**|**Usage**|**Parameters**|
|:------------|:--------|:-------------|
|put|insert a new key-value pair, or replace an old key-value pair|key and value|
|get|get the value of the indicated key |key|
|bulkPut|inset 2 or more key-value pairs together|map of key and value|
|bulkGet|get the value of 2 or more indicated keys |list of keys|
|prefix|get the value of the keys with the indicated prefix string|prefix and number of result|
|bulkPrefix|get the value of the keys with the indicated prefix strings|list of prefix, number of result, beginning key, sort the key-value pairs in ascending or descending order|
|kmax|get the k maximum  key-value pairs|k |
|kmin|get the k minimum key-value pairs|k |
|range|get the key-value pairs whose key is between the indicated keys|beginning key and ending key|
|before|get the key-value pairs whose key is before the indicated key in alphabetical order|key and number of result|
|after|get the key-value pairs whose key is after the indicated key in alphabetical order|key and number of result|
|length|get the number of key-value pairs|none|
|remove|delete the indicated key-value pair|key|
|removePrefix|delete value of the keys with indicated prefix string|prefix|
|optimize|optimize the space usage of index file|number of nodes need to be optimized(1024 is recommended value)|


## Sample ##


> http://code.google.com/p/treapdb/wiki/SomeBenchmark

## RPC Server ##

  * memcached protocol compatible server for **test usage**
  * thrift-based server for production usage

## Download & Configure ##
  * click here to download http://code.google.com/p/treapdb/downloads/list

  * run
    * ./treapdb.sh conf/TreapDBConf.xml(master mode)
    * ./treapdb.sh conf/TreapDBConf\_Slave.xml(slave mode)

  * configuration:

> TreapDBConf.xml

```
     
      <?xml version="1.0" encoding="UTF-8"?>
      <TreapDB>
	<Params>
		<Port>
			<Memcache>11811</Memcache> 
                         //Listening port of memcache protocol
			<Thrift>11812</Thrift>     
                         //Listening port of thrift protocol
		</Port>
		<Index>
			<FilePath>/var/log/treapdb/master</FilePath> 
                          //Index file name
			<BlockSize>64</BlockSize>  
                          //size of index-block-item;
                          //default key length is 26(=64-38),38 is the node-overhead, 
                          //TreapDB supports max key-length: 127 bytes)

		</Index>
		<MMapSize>128</MMapSize> //the more the better, 128MB is enough for 2 Million keys
		<Replication>
			<Role>Master</Role>
		</Replication>
	</Params>
       </TreapDB>
```


> TreapDBConf\_Slave.xml

```
       <?xml version="1.0" encoding="UTF-8"?>
       <TreapDB>
	 <Params>
		<Port>
			<Memcache>11911</Memcache>
			<Thrift>11912</Thrift>
		</Port>
		<Index>
			<FilePath>/var/log/treapdb/slave</FilePath>
			<BlockSize>64</BlockSize>
		</Index>
		<MMapSize>128</MMapSize>
		<Replication>
			<Role>Slave</Role>
			<Source>10.61.1.170:11811</Source>
                       //Set master address and port here!
		</Replication>
	 </Params>
       </TreapDB>


```
## How to build from source ##

  1. svn checkout http://treapdb.googlecode.com/svn/trunk/
  1. ant dist
  1. use the jars generated in build/dist

## How to write client code ##
> Java:
```
     String host = "localhost";
     Integer port = 11812; //thrift port
     TreapDBClient client = TreapDBClientFactory.getClient(host, port);
     client.put(...)
     client.get(...)
     client.prefix(...)
     ...
```
> Python:
```
     import pytreap
     client = pytreap.connect('localhost',11812) //thrift port
     client.get(...)
     client.put(...)
     client.remove(...)
```

## Can TreapDB be used in an embedded way? ##

> Yes,put libtreap-xx.jar in your app's classpath.
```
    DiskTreap<String, Serializable> treap = new DiskTreap<String,Serializable>
(new File("/usr/local/indexpath")); 

    treap.put(...)
    treap.get(...)
    ...

    /* key can be any type which implements Comparable;
    value can be any type which implements Serializable*/
```

## Only Java? ##

  * TreapDB server is based on thrift, so you can generate your client in
any programming language.

  * For example, generate the python client code.
> > thrift --gen py res/service.txt
  * res/service.txt is a service specification:



> Python client: http://code.google.com/p/treapdb/wiki/PythonClientExample

## What is treap? ##

> In computer science, the treap and the randomized binary search tree are two closely-related forms of binary search tree data structures that maintain a dynamic set of ordered keys and allow binary searches among the keys.
> After any sequence of insertions and deletions of keys, the shape of the tree is a random variable with the same probability distribution as a random binary tree; in particular, with high probability its height is proportional to the logarithm of the number of keys, so that each search, insertion, or deletion operation takes logarithmic time to perform.

http://en.wikipedia.org/wiki/Treap

## Contact the author ##
  * Junyi Sun, Qiang Ma
  * E-mail: ccnusjy (#$) gmail.com
  * Sponsor: [Sino-German Joint Laboratory of Software Integration](http://sigsit.ict.ac.cn/)

Welcome your valuable suggestions. My microblog: http://www.weibo.com/treapdb