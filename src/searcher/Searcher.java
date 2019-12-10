/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searcher;

import java.io.File;
import java.io.IOException;
import static java.lang.System.exit;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author dwaipayan
 */
public class Searcher {

    String indexPath;
    String query;
    String searchField;

    //      import org.apache.lucene.analysis.Analyzer;
    // same analyzer that has been used while indexing
    Analyzer analyzer;

    QueryParser queryParser;
    //      import org.apache.lucene.search.IndexSearcher;
    IndexSearcher searcher;
    //      import org.apache.lucene.index.IndexReader;
    IndexReader reader; 
    // we need to have an IndexReader for searching the index as well
    
    String stopwordPath;
    List<String> stopwordList;

    public Searcher(String indexPath, String query, String searchField) throws IOException {

        this.indexPath = indexPath;
        this.query = query;
        this.searchField = searchField;

        //      import org.apache.lucene.analysis.en.EnglishAnalyzer;
        // in case to use the default stopword list
        analyzer = new EnglishAnalyzer();

        queryParser = new QueryParser(searchField, analyzer);

        //      import org.apache.lucene.store.Directory;
        Directory indexDir;
        //      import org.apache.lucene.store.FSDirectory;
        // FSDirectory.open(file-path-of-the-dir)
        indexDir = FSDirectory.open((new File(this.indexPath)).toPath());
        //      import org.apache.lucene.index.DirectoryReader;
        reader = DirectoryReader.open(indexDir);
        searcher = new IndexSearcher(reader);
        //      import org.apache.lucene.search.similarities.BM25Similarity;
//        searcher.setSimilarity(new BM25Similarity());
        searcher.setSimilarity(new KLDivergenceSimilarity());
    }

    public Query makeLuceneQuery(String queryStr) throws ParseException {

        Query luceneQuery = queryParser.parse(queryStr);
        return luceneQuery;
    }

    public void search(String queryStr) throws ParseException, IOException {

        //      import org.apache.lucene.search.ScoreDoc;
        ScoreDoc[] hits;

        //      import org.apache.lucene.search.TopDocs;
        TopDocs topDocs;

        //      import org.apache.lucene.search.TopScoreDocCollector;
        TopScoreDocCollector collector = TopScoreDocCollector.create(10);

        Query luceneQuery = makeLuceneQuery(queryStr);

        // the actual search is taking place
        searcher.search(luceneQuery, collector);

        topDocs = collector.topDocs();
        hits = topDocs.scoreDocs;
        if(hits == null)
            System.out.println("Nothing found");
        
        int rank = 0;
        for (ScoreDoc hit : hits) {
            int luceneDocid = hit.doc;
            Document d = searcher.doc(luceneDocid);
            System.out.println(++rank + " - \t" + hit.score + ":\t" + d.get("title"));// + " : "+ d.get("summary"));
        }
    }

    public static void main(String[] args) throws ParseException, IOException {

        String indexPath, queryStr, searchField;
        args = new String[3];
        args[0] = indexPath = "/home/dwaipayan/LuceneDemo.index";
        args[1] = queryStr = "war peace";
        args[2] = searchField = "summary";
        if(args.length!=3) {
            System.out.println("Usage: java searcher.Searcher <index-path> <query> <searching-field>");
            exit(0);
        }
        indexPath = args[0];
        queryStr = args[1];
        searchField = args[2];

        Searcher searcher;
        searcher = new Searcher(indexPath, queryStr, searchField);
        searcher.search(queryStr);
    }
}
