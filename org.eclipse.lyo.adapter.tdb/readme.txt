if querying the triplestore, or retrieving resources from the triplestore, or renaming a resource 

causes a java.util.ConcurrentModificationException: Iterator: started at 5, now 89 
check if a tdb.lock file exists in the triplestore folder and delete it!

causes org.apache.jena.atlas.lib.InternalErrorException: Invalid id node for object (null node): ([0000000000000000], [0000000000000153], [000000000001BA85])
restart Eclipse and delete tdb.lock file  in the triplestore folder
or safer: just query the triplestore through SPARQL (separate process)

Check out https://jena.apache.org/documentation/notes/concurrency-howto.html