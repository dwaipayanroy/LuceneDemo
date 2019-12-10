/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author dwaipayan
 */
public class Indexer {

    String collectionPath;
    String indexPath;

    int docCount;

    //      import org.apache.lucene.index.IndexWriter;
    IndexWriter writer;
    //      import org.apache.lucene.analysis.Analyzer;
    // same analyzer to be used while searching
    Analyzer analyzer;

    String stopwordPath;
    List<String> stopwordList;
    
    public Indexer(String collectionPath, String indexPath) throws IOException {

        this.collectionPath = collectionPath;
        this.indexPath = indexPath;

        //      import org.apache.lucene.analysis.en.EnglishAnalyzer;
        // in case to use the default stopword list
        analyzer = new EnglishAnalyzer();
        // in case to use own stopword list
//        stopwordList = getStopwordList(stopwordPath);
        //      import org.apache.lucene.analysis.core.StopFilter;
//        analyzer = new EnglishAnalyzer(StopFilter.makeStopSet(stopwordList));
        // in general:
        //      import org.apache.lucene.analysis.ln.LanguageAnalyzer;
        //      analyzer = new LanguageAnalyzer(StopFilter.makeStopSet(stopwordList));

        // e.g. German Analyzer:
        //      import org.apache.lucene.analysis.de.GermanAnalyzer;
        //      analyzer = new GermanAnalyzer();

        //writer = new IndexWriter(lucene-fs-directory-to-store-index, index-writer-config)

        //      import org.apache.lucene.store.Directory;
        Directory indexDir;
        //      import org.apache.lucene.store.FSDirectory;
        // FSDirectory.open(file-path-of-the-dir)
        indexDir = FSDirectory.open((new File(this.indexPath)).toPath());

        //      import org.apache.lucene.index.IndexWriterConfig;
        IndexWriterConfig iwcfg;
        iwcfg = new IndexWriterConfig(analyzer);
        iwcfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE);   // other options: APPEND, CREATE_OR_APPEND

        writer = new IndexWriter(indexDir, iwcfg);
        docCount = 0;
    }

    public static void main(String[] args) throws IOException {

        String collectionPath, indexPath;
        if(args.length!=2) {
            System.out.println("Usage: java indexer.Indexer <collection-path> <index-path>");
            exit(0);
        }
//        args = new String[2];
//        args[0] = "/home/dwaipayan/Dropbox/movieSummaries.tsv";
//        args[1] = "/home/dwaipayan/LuceneDemo.index";
        collectionPath = args[0];
        indexPath = args[1];
        Indexer indexer = new Indexer(collectionPath, indexPath);

        indexer.createIndex(indexer.collectionPath);
    }

    private List<String> getStopwordList(String stopwordPath) {
        List<String> stopwords = new ArrayList<>();

        String line;
        try {
            System.out.println("Stopword Path: "+stopwordPath);
            FileReader fr = new FileReader(stopwordPath);
            BufferedReader br = new BufferedReader(fr);
            while ( (line = br.readLine()) != null )
                stopwords.add(line.trim());

            br.close(); fr.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Error: \n"
                + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n"
                + "Stopword file not found in: "+stopwordPath);
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error: \n"
                + "StandardAnalyzerWithSmartStopword: setAnalyzer()\n"
                + "IOException occurs");
            System.exit(1);
        }

        return stopwords;
    }

    private void createIndex(String collectionPath) throws FileNotFoundException, IOException {
        System.out.println("Indexing started...");

        File colFile = new File(collectionPath);

        if(colFile.isDirectory())
            indexDirectory(colFile);
        else
            indexFile(colFile);
    }

    private void indexDirectory(File collDir) throws FileNotFoundException, IOException {
        File[] files = collDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                indexDirectory(file);  // recurse
            }
            else {
                indexFile(file);
            }
        }

    }

    private void indexFile(File colFile) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(colFile));
        String line;

        //      import org.apache.lucene.document.Document;
        Document doc;

        while((line=br.readLine())!=null) {

            doc = parseTSV(line);

            writer.addDocument(doc);
            System.out.println(++docCount + " " + line.split("\t")[1]);
        }
        writer.close();
    }

    private Document parseTSV(String line) {
        String fields[];
        fields = line.split("\t");

        Document doc = new Document();

        //      import org.apache.lucene.document.StringField;
        //      new IntField(field-name, field-value, Field.Store.YES)
        doc.add(new IntField("docid", Integer.parseInt(fields[0]), Field.Store.YES));
        //      new StringField(field-name, field-value, Field.Store.YES)
        doc.add(new Field("title", fields[1], Field.Store.YES, Field.Index.ANALYZED));
        // if you want to store the field in a searchable way, use Field()
        doc.add(new Field("summary", fields[2], Field.Store.YES, Field.Index.ANALYZED));

        return doc;
    }
}
