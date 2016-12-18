package ru.otr.nzx.extra.dumpsearch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

import cxc.jex.postprocessing.Action;
import cxc.jex.tracer.Tracer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import ru.otr.nzx.config.http.location.LocationConfig;
import ru.otr.nzx.http.Processor;
import ru.otr.nzx.http.location.Location;
import ru.otr.nzx.postprocessing.Dumping;
import ru.otr.nzx.postprocessing.NZXPostProcessor;
import ru.otr.nzx.postprocessing.NZXTank;

public class DumpSearchProcessor extends Processor {

    private DumpSearchProcessorConfig config;

    private Analyzer analyzer;
    private Directory directory;
    private IndexWriter ixWriter;

    public DumpSearchProcessor(LocationConfig locationConfig, NZXPostProcessor postProcessor, Tracer tracer) {
        super(locationConfig, postProcessor, tracer.getSubtracer("#DumpSearchProcessor"));
        config = new DumpSearchProcessorConfig(locationConfig.processor_parameters);
    }

    @Override
    public void bootstrap() {
        tracer.info("Bootstrap", "Index directory [" + config.search_index.getPath() + "]");
        if (!config.dumps_store.exists() && !config.dumps_store.mkdirs()) {
            throw new RuntimeException("Cannot make directory [" + config.dumps_store.getPath() + "]");
        }
        if (!config.search_index.exists() && !config.search_index.mkdirs()) {
            throw new RuntimeException("Cannot make directory [" + config.search_index.getPath() + "]");
        }
        try {
            analyzer = new StandardAnalyzer();
            directory = FSDirectory.open(config.search_index.toPath());
            IndexWriterConfig ixwConfig = new IndexWriterConfig(analyzer);
            if (config.reindex_on_start) {
                ixwConfig.setOpenMode(OpenMode.CREATE);
            } else {
                ixwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }
            ixWriter = new IndexWriter(directory, ixwConfig);
            if (config.reindex_on_start) {
                indexDumps();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() {
        tracer.info("Starting", "");

    }

    @Override
    public void stop() {
        try {
            ixWriter.commit();
            ixWriter.close();
            directory.close();
        } catch (IOException e) {
            tracer.error("Lucene.Error/NOTIFY_ADMIN", e.getMessage(), e);
        }
        tracer.info("Stopped", "");
    }

    private void indexDumps() throws IOException {
        tracer.info("Reindex.Begin", "");
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (DumpSearchProcessorConfig.CONST_SEARCH_INDEX.equals(name)) {
                    return false;
                } else {
                    return true;
                }
            }
        };
        for (File location : config.dumps_store.listFiles(filter)) {
            for (File date : location.listFiles()) {
                tracer.debug("Reindex", location.getName() + "/" + date.getName());
                for (File dump : date.listFiles()) {
                    String path = location.getName() + "/" + date.getName() + "/" + dump.getName();
                    String content = new String(Files.readAllBytes(dump.toPath()));
                    indexDump(path, content);
                }
                ixWriter.flush();
            }
        }
        ixWriter.commit();
        tracer.info("Reindex.End", "");
    }

    private void indexDump(String path, String content) throws IOException {
        StringBuilder cleanContent = new StringBuilder();
        for (String term : content.toString().split(" ")) {
            if (term.length() < 1024) {
                cleanContent.append(term);
                cleanContent.append(" ");
            }
        }
        Document doc = new Document();
        doc.add(new Field(DumpSearchProcessorConfig.CONST_PATH, path, TextField.TYPE_STORED));
        doc.add(new Field(DumpSearchProcessorConfig.CONST_CONTENT, cleanContent.toString(), TextField.TYPE_STORED));
        ixWriter.addDocument(doc);
    }

    public void indexDump(NZXTank tank) throws IllegalAccessException, IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            tank.getBuffer().read(baos);
            indexDump(Dumping.makePath(tank), baos.toString());
            ixWriter.commit();
        }
    }

    @Override
    public Action<NZXTank> makeAction() {
        return new DumpIndexing(this);
    }

    @Override
    public Location makeLocation(HttpRequest originalRequest, ChannelHandlerContext ctx, Date requestDateTime, String requestID, URI requestURI) {
        return new DumpSearchLocation(this, originalRequest, ctx, requestDateTime, requestID, requestURI, locationConfig, postProcessor, tracer);
    }

    public List<String> search(String queryText) throws IOException {
        List<String> result = new ArrayList<>();
        DirectoryReader dReader = DirectoryReader.open(directory);
        IndexSearcher ixSearcher = new IndexSearcher(dReader);
        QueryBuilder qBuilder = new QueryBuilder(analyzer);
        Query query = qBuilder.createPhraseQuery(DumpSearchProcessorConfig.CONST_CONTENT, queryText);
        ScoreDoc[] hits = ixSearcher.search(query, 1000).scoreDocs;
        for (ScoreDoc item : hits) {
            result.add(config.ftp_server + "/" + ixSearcher.doc(item.doc).get(DumpSearchProcessorConfig.CONST_PATH));
        }
        dReader.close();
        return result;
    }
}
