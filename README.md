# NCDSearch

A grep-like tool to find similar source code fragments using Normalized Compression Distance.

Normalized Compression Distance (https://en.wikipedia.org/wiki/Normalized_compression_distance) is defined as follows:

        NCD(x, y) = (Z(xy)-min(Z(x), Z(y)) / max(Z(x), Z(y))

where Z(x), Z(y), and Z(xy) are data size obtained by a data compression algorithm (Deflate in our implementation).
If two data `x` and `y` are similar, then NCD(x, y) results in a small value.

For example, if a line `if (this.distance < another.distance) return true;` (in source code of the tool) is given as a query, the tool reports similar lines such as 
`if (this.distance > another.distance) return false;` and `if (thislen > anotherlen) return true;`.
The tool assumes that either a long identifier or a few lines of code as a query.



## Usage

### Search a code snippet

You can input code fragments using STDIN, a query file, or command line arguments.

        java -jar ncdsearch.jar dir_or_file -lang java < query
        java -jar ncdsearch.jar dir_or_file -lang java -q query.txt
        java -jar ncdsearch.jar dir_or_file -lang java -e my code fragments

Note that all the arguments after `-e` are regarded as a query code snippet.

You can specify multiple directories or files to be searched.
        java -jar ncdsearch.jar dir1 dir2 -lang java < query


### Programming Language

The `-lang` option specifies a programming language.  
The tool accepts file extensions: `java` (Java), `c` (C/C++), `cs` (C#), and `js` (JavaScript).  


### Full Scan Mode

For efficiency, the tool compares a query with sampled lines of code by default.  It is fast, but may result in false negatives.
If your query is small enough, you should specify `-full` option that checks all tokens so that you can get more results.
        java -jar ncdsearch.jar dir_or_file -lang java -full -e identifier


### Verbose Mode

If a result is different from your expectation, you can try `-v` to see the configuration and progress of the search.
        java -jar ncdsearch.jar dir_or_file -lang java -v < query


### Compression Algorithm

You can choose a compression algorithm other than Deflate.
The tool accepts `XZ` and `ZSTD` that are corresponding to Xz (https://tukaani.org/xz/java.html) and Zstd (https://github.com/luben/zstd-jni) algorithms.

        java -jar ncdsearch.jar dir_or_file -lang java -c XZ < query

The feature is experimental to see the dependency of compression algorithms.
