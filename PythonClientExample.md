# Use python to connect TreapDB server #

http://code.google.com/p/treapdb/source/browse/#svn/trunk/res/python-client-example


# Details #

use [pytreap](http://treapdb.googlecode.com/files/python_client_1.0.zip)

```
import pytreap
def main():
    client = pytreap.connect('localhost',11812)
    data = 'abc'*30
    for i in xrange(100):
        client.put(str(i),data)
    for i in xrange(10):
        print i,'=>',client.get(str(i))
    results = client.prefix('9',5,None,True) #fetch keys that startswith '9',at most 5 entries, asc order
    print results
    client.close()
    
if __name__ == "__main__":
    main()
```