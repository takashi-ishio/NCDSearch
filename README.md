# NCDSearch

NCDSearch is a grep-like tool to find similar source code fragments in files.
In software development, developers may copy and modify source code fragments to reuse existing functionalities in code. 
This tool is developed to detect such code fragments that are not identified by a simple search tool like grep.

For example, if a line `if (this.distance < another.distance) return true;` (in source code of the tool) is given as a query, the tool reports similar lines such as 
`if (this.distance > another.distance) return false;` and `if (thislen > anotherlen) return true;`.
You can find two bug fix examples in a company in [our paper published in ICSME 2018 Industry Track](NCDSearch_motivation_2018.pdf).
> Takashi Ishio, Naoto Maeda, Kensuke Shibuya, Katsuro Inoue: 
> Cloned Buggy Code Detection in Practice Using Normalized Compression Distance.
> In proceedings of ICSME 2018, pp.591-594, November 2018.

The tool supports famous similarity metrics such as Normalized Compression Distance and Normalized Levenshtein Distance.
The default search strategy is Lempel-Ziv Jaccard Distance (proposed in <https://arxiv.org/abs/1708.03346>).
It is significantly faster than existing NCD, while it keeps similar output to NCD using the Deflate (zip) algorithm for code clone detection.
The details are described in [our technical paper](NCDSearch_evaluation_2022.pdf). 
> Takashi Ishio, Naoto Maeda, Kensuke Shibuya, Kenho Iwamoto, Katsuro Inoue,
> NCDSearch: Sliding Window-Based Code Clone Search Using Lempel-Ziv Jaccard Distance.
> IEICE Transactions on Information and Systems, vol.E105-D, No.5, May 2022.



## Build Information

The project uses Maven with Maven Assembly Plugin.
The following command builds a runnable jar `ncdsearch.jar`:

         mvn package

The project  includes Eclipse project file and `pom.xml` for dependencies. 
The main class is `ncdsearch.SearchMain`.
The repository includes Lexer files generated by ANTLR4.
The grammar files are maintained in another repository (https://github.com/takashi-ishio/sarf-lexer) 


## Usage

Online usage example is available at:
 - (English) https://colab.research.google.com/drive/1RRQS8jQWMIi2fdXmiTBcbwNB3FeZ74Tr
 - (Japanese) https://colab.research.google.com/drive/1eL_92HCCWfVNPRPfc5msi7H_-v5jsx-G


The following table is a list of major options. 

|Category       | Option                  |Description                                                             |
|:-----------|:-----------------------|:-----------------------------------------------------------------------|
|[Query](#query-options)|`-e` [query]            |Specify a query. All arguments after the option are regarded as a query.|
|               |`-q` [filename]         |Read a query from a specified file.  `-` represents a standard input.   | 
|               |`-sline` [line-number]  |Specify a start line number of a query in the file of `-q` option.      |
|               |`-eline` [line-number]  |Specify an end line number of a query in the file of `-q` option.       |
|               |`-lang` [language]      |Specify a programming language of a query.  Required for `-e` option.   |
|[Target file](#target-file-options)| [src]                   |Specify a source file or directory name.                               |
|               |`-targetlang` [language]|Specify a programming language for target files.                      |
|               |`-i` [extension]         |Specify a file extension to include in the search.                     |
|               |`-encoding` [encoding]  |Specify a text encoding of source files.  The default encoding is UTF-8.|
|               |`-l` [filelist]         |Search files listed in the specified text file (one file name per line) |
|               |`-git` [git-dir]        |Search files in the specified git repository instead of normal files.   |
|               |`-gitcommit` [commit]   |Specifies a commit to be analyzed when `-git` option is specified.  The default value is "HEAD".|
|[Output](#output-options)|`-v`                    |Show configuration and progress.                                        |
|               |`-sort`                 |Report fragments in the ascending order of distance.                    |
|               |`-time`                 |Report the ellapsed time and the numbers of analyzed files and lines.   |
|               |`-json`                 |Enable a JSON format report.                                            |
|               |`-pos`                  |Report the detected source code locations in detail.                    |
|               |`-link` [style]         |If one of `eclipse`, `vscode`, and `fileurl` is given, file names are printed using a clickable format on particular environments.  The default is `none` (just a file name). |
|[Strategy](#strategy-options)|`-a` [algorithm]        |Specify an algorithm to compute a similarity. The default is `lzjd`.   |
|               |`-th` [threshold]       |Specify a threshold of the distance.  The default is 0.5.               |
|               |`-thread` [num]         |Specify the number of threads for concurrent search.                    ||
|[Troubleshooting](#troubleshooting-options)|`-testconfig`           |This option does not execute a search but print the current configuration.|


### Query options

You can input code fragments using STDIN, a query file, or command line arguments.

        java -jar ncdsearch.jar dir_or_file -lang java -q - < query
        java -jar ncdsearch.jar dir_or_file -lang java -q query.txt
        java -jar ncdsearch.jar dir_or_file -lang java -q query.txt -sline 10 -eline 20
        java -jar ncdsearch.jar dir_or_file -lang java -e my code fragments

 - The `-q` option specifies a file for a code fragment.  `-sline` and `-eline` specifies lines of code in a file as a query.  In the third example, lines 10 through 20 of the query.txt file are selected as a query.  
   - If a file name is "-", the tool reads a query from the standard input.
 - The `-e` option directly specifies a code fragment in command line arguments.
   Note that all the arguments after `-e` are regarded as a query code snippet.

You can specify multiple directories or files to be searched.

        java -jar ncdsearch.jar dir1 dir2 -lang java < query

The tool recognizes a programming language by either `-lang` option or a file name of `-q` option.  The `-lang` option overrides `-q` option's file name.


It should be noted that the tool reports all similar code fragments by default.
If you would like to see only exact matches, you can use `-a tld` option that uses Token-level Levenshtein Distance instead of the default strategy.

        java -jar ncdsearch.jar dir_or_file -lang java -e public static void main(String[] args)


### Target file options

The tool accepts file and directory names to search as arguments.

#### Source File Encoding

The tool assumes UTF-8 by default.
Please specify `-encoding` option to choose a charset, e.g. `-encoding UTF-16`.
A list of supported encodings is dependent on a platform.  
A list for Oracle Java SE is available at: <https://docs.oracle.com/javase/jp/8/docs/technotes/guides/intl/encoding.doc.html>


#### Programming Languages

The `-lang` option specifies a programming language.  
The tool uses file extensions as programming language names: `java` (Java), `c` (C/C++), `cs` (C#), `js` (JavaScript), `cbl` (Cobol), `vb` (Visual Basic 6), `txt` (plain text), and `generic` (generic tokenizer).  
It also accepts `ccfxprep` files that are generated by the CCFinderX  preprocessor. 

 - A programming language option activates a lexical analysis to extract tokens.  It ignores white space and comments in the specified language.
 - The plain text mode regards a single character as a single token but ignores indentation (leading white space and trailing white space of lines). 
 - The generic tokenizer regards a single word (alphabets and numeric characters) and a non-word symbol as a token.  This option enables you to analyze source code that are unsupported by this tool.  

Each language option automatically searches source files using the following extensions.
 - C/C++: .c, .cc, .cp, .cpp, .cx, .cxx, .c+, .c++, .h, .hh, .hxx, .h+, .h++, .hp, .hpp
 - Java 8: .java
 - JavaScript: .js
 - C#: .cs
 - Python: .py
 - COBOL: .cbl
 - Visual Basic 6 and .NET (experimental): .vb 
 - Plain Text: .txt, .html, .md
 - Docx files: .docx
 - CCFinderX: .ccfx, .ccfinderx, .ccfxprep
 - Generic: .generic, .neutral

You can include additional files using `-i (extension)` option.
A combination of `-lang` and `-i` option enables to choose any lexical analysis.
For example, the following command searches .java files as plain text.

        java -jar ncdsearch.jar dir_or_file -lang txt -i .java -e "// line comment"

The following command is to analyze Rust source files usign a generic tokenizer.

        java -jar ncdsearch.jar dir_or_file -lang generic -i .rs -e "fn main() {"

Differently from supported languages, the generic tokenizer includes code comments in search results.

The file extension is case insensitive.  You can use multiple `-i` options in a command line to search additional files. 

If a query language is different from target files (e.g., a query file is .c but you want to search Java source files), 
you can use `-targetlang` option.  The following command regards a query code fragment as a C code fragment, while it searches Java files.

        java -jar ncdsearch.jar dir_or_file -lang c -targetlang java -e "int main(void)"

In addition to `-i` option, you can use `-l [filename]` option to specify a text file including a list of file names.


### Output options

The tool reports a result in a CSV format by default.
For example, an execution with a query:

        java -jar ncdsearch.jar -lang java -e "if (this.distance > another.distance) return true;"

would report a line like this:

        path/to/src/ncdsearch/Fragment.java,81,81,0.10714285714285714

Each line of an output represents a similar source code fragment detected by the tool.
  * The first column is the file name including the code fragment. 
  * The second and third columns indicate the lines of the first and last tokens of the fragment. 
    * You may specify `-pos` option to extract char positions in the lines. 
  * The last column indicates the normalized compression distance between the query and the code fragment.  Since it is a distance, more similar code fragments have smaller values.

According to the report, you may find a similar line of code in a file. For example:

        81:   if (this.distance < another.distance) return true;

The tool also supports a JSON format.  Add `-json` option to use the format.
The `-pos` option with the json option reports source code tokens in addition to locations.


#### Verbose Mode

If a result is different from your expectation, you can try `-v` to see the configuration and progress of the search.

        java -jar ncdsearch.jar dir_or_file -lang java -v < query


### Strategy options

You can specify an algorithm using `-a` option.
This tool supports the following algorithms.

 - `lzjd`: The default strategy of the tool.  An efficient implementation of Lempel-Ziv Jaccard Distance.  
 - `lzjd2022`: An implementation of Lempel-Ziv Jaccard Distance described in the [technical paper](NCDSearch_evaluation_2022.pdf).
 - `clzjd`: Character-level Lempel-Ziv Jaccard Distance.  It internally uses `char` instead of `byte` for comparison.  This may be important to find similar multi-byte strings.
 - `tld`: Token-level Levenshtein Distance.  A distance is measured by the number of add/delete/modify operations to transform a query string to a code fragment in source code.
 - `ntld`: Normalized Token-level Levenshtein Distance.  A distance is a variant of Token-level Levenshtein Distance normalized by the length of a query string.
 - `nbld`: Normalized Byte-level Levenshtein Distance.  A distance is measured by the number of different bytes, normalized by the length of a query string..
 - `blcs`: Normalized Byte-level Longest Common Subsequence.  A distance is measured by the length of common byte subsequence normalized by the length of a query string.
 - `zip`: Normalized Compression Distance with Deflate algorithm that has been used in gzip.
 - `xz`, `zstd`, `bzip2`, `snappy`, `folca`: Normalized Compression Distance with the corresponding compression algorithm. These algorithms are provided just for experiments to see an impact of compression algorithms.

#### Normalized Compression Distance (NCD)

Using the `-a zip` option, you can use a traditional Normalized Compression Distance (https://en.wikipedia.org/wiki/Normalized_compression_distance) defined as follows:

        NCD(x, y) = (Z(xy)-min(Z(x), Z(y)) / max(Z(x), Z(y))

where Z(x), Z(y), and Z(xy) are data size obtained by a data compression algorithm (Deflate in case of the `zip` option).
If two data `x` and `y` are similar, then NCD(x, y) results in a small value.


#### Token-level Levenshtein Distance

The `-a ntld` option uses Normalized Levenshtein Distance on tokens, i.e. the ratio of added, removed, and modified tokens, as a distance metric.

The `-a tld` option uses Levenshtein Distance on tokens without normalization.
It simply counts the number of added, removed, and modified tokens and reports a code fragment whose distance is at most a given threshold.
For example, `-q "a < b" -th 1 -full` matches `a > b` and  `a < c`.

#### Multi-threaded search

The program uses N working threads if `-thread N` option is provided.
The multi-threading execution processes N files in parallel.
File locations in the output may be differently ordered in each execution.

Although N can be an arbitrary number (e.g. 2, 4, or 8), an effective value of N is dependent on available CPU resources. 
A larger amount of memory is also required to store N files in memory at once.


### Troubleshooting options

The `-testconfig` option prints the current configuration as similar to `-v`, but it does not proceed to an actual search step.
The option can be used to check how command line options are recognized by the tool.

### Experimental options

#### Full Scan Mode

The tool starts comparisons from the beginning of each line.  While it is fast, it may result in false negatives.
By specifying the `-full` option, similar code starting from the middle of a line can also be detected.
This option should only be used with short queries.

        java -jar ncdsearch.jar dir_or_file -lang java -full -e identifier


## Evaluation Dataset

The `evaluation-dataset/cbcd-dataset.json` file includes 53 queries created from the information in [J. Li and M. D. Ernst: CBCD: Cloned Buggy Code Detector](https://homes.cs.washington.edu/~mernst/pubs/buggy-clones-tr110502.pdf).  
As we found several errors in the paper, we manually checked the version history of three projects and revised the queries and answers.

Each object includes a query source code fragment and code clones that should be detected.
 - `query`: A file name including the query content.
 - `queryloc`: The source code location of the query.
   - `path` shows a project name (postgres/git/linux) and a commit ID including the code.
   - `file`, `sline`, and `eline` represent the file path, start line number and end line number in the version.
 - `type`: Code clone type.
 - `path`: A project name and a commit ID including code clones.
 - `answers`: A list of source code locations.  The format is the same as `queryloc`.


## License

The source code of this project is available under the MIT License.
The full license description is included in the LICENSE file.

 - GNU Trove (https://bitbucket.org/trove4j/trove): LGPL License
 - fastutil (https://fastutil.di.unimi.it/): Apache License 2.0
 - ANTLR4 (http://www.antlr.org/license.html): BSD License
 - JUnit 4 (https://junit.org/junit4/): Eclipse Public License 1.0
 - Eclipse JGit (https://projects.eclipse.org/projects/technology.jgit): Eclipse Distribution License 1.0 (BSD-3-Clause)
 - Jackson Core (https://github.com/FasterXML/jackson-core): Apache License 2.0
 - Jackson Databind (https://github.com/FasterXML/jackson-databind): Apache License 2.0
 - Apache POI (https://poi.apache.org/): Apache License 2.0

This project also uses the following components for normalized compression distance.

 - Commons Compress (https://commons.apache.org/proper/commons-compress/): Apache License 2.0
 - XZ (https://tukaani.org/xz/java.html): public domain 
 - ZStd (https://github.com/luben/zstd-jni): 3-clause BSD License 
 - Snappy-Java (https://github.com/xerial/snappy-java): Apache License 2.0

Grammar files for ANTLR4 come from https://github.com/antlr/grammars-v4/ except for COBOL.
C/C++ grammars are slightly edited so that the tool can recognize macro directives as tokens (see the header comment).
COBOL grammar is obtained from ProLeap COBOL Parser (https://github.com/uwol/proleap-cobol-parser) that is distributed under MIT License.
VisualBasic6 grammar is obtained from ProLeap VB6 Parser (https://github.com/uwol/vb6parser) that is distributed under MIT License.

