package com.company.distributed.framework.worker;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class WorkerNode
{
    public static void main(String[] args) throws Exception
    {
//        String clientId = UUID.randomUUID().toString();
//
//        String ipAddress = args[1];
//        String port = args[2];
//        String numThreads = args[3];
//        String heartBeatFreqMillis = args[4];
//
//
//
//        // TODO: Register worker node with master node

        HeartbeatService hbService = new HeartbeatService(UUID.randomUUID().toString());
        hbService.startService();

        // send join request and wait for it.


    }

    public static class WordCount
    {
        private final String m_strFileName;
        private final long m_lStartOffset;
        private final int m_iChunkSize;

        private final ExecutorService m_executorService;
        private final int m_iThreadPoolSize;

        public WordCount(String strFileName, long lStartOffset, int iChunkSize,
                         ExecutorService executorService, int iThreadPoolSize)
        {
            m_strFileName = strFileName;
            m_lStartOffset = lStartOffset;
            m_iChunkSize = iChunkSize;
            m_executorService = executorService;
            m_iThreadPoolSize = iThreadPoolSize;
        }

        public WordCountResult count() throws Exception
        {
            FileInputStream fis = new FileInputStream(m_strFileName);
            FileChannel fc = fis.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, m_lStartOffset, m_lStartOffset + m_iChunkSize);

            int mod = m_iChunkSize % m_iThreadPoolSize;

            List<Future<WordCountResult>> listFuture = new ArrayList<>();

            int partitionSize = m_iChunkSize/m_iThreadPoolSize;
            for (int i = 0; i < m_iThreadPoolSize; i++)
            {
                if (i == m_iThreadPoolSize - 1)
                {
                    partitionSize += mod;
                }

                WordCountTask wordCountTask = new WordCountTask(bb.slice(i * partitionSize, partitionSize));
                listFuture.add(m_executorService.submit(wordCountTask));
            }

            fc.close();
            fis.close();

            char firstChar = listFuture.get(0).get().m_cFirstChar;
            char lastChar = listFuture.get(0).get().m_cLastChar;

            long wordCount = 0;

            boolean isPrevCharWhiteSpace = false;

            for (Future<WordCountResult> future: listFuture)
            {
                WordCountResult wordCountResult = future.get();
                boolean isCurrCharWhiteSpace = Character.isWhitespace(wordCountResult.m_cFirstChar);

                if (isPrevCharWhiteSpace && !isCurrCharWhiteSpace)
                {
                    wordCount++;
                }

                wordCount += wordCountResult.m_lWordCount;

                isPrevCharWhiteSpace = Character.isWhitespace(wordCountResult.m_cLastChar);
                lastChar = wordCountResult.m_cLastChar;
            }

            return new WordCountResult(firstChar, lastChar, wordCount);
        }
    }

    public static class WordCountTask implements Callable<WordCountResult>
    {
        private final ByteBuffer m_bb;

        public WordCountTask(ByteBuffer bb)
        {
            m_bb = bb;
        }

        @Override
        public WordCountResult call() throws Exception
        {
            int position = m_bb.position();
            char firstChar = (char) m_bb.get();
            char lastChar = firstChar;
            m_bb.position(position);

            long wordCount = 0;

            boolean isPrevCharWhiteSpace = false;

            while (m_bb.hasRemaining())
            {
                char currChar = (char) m_bb.get();
                boolean isCurrCharWhiteSpace = Character.isWhitespace(currChar);

                if (isPrevCharWhiteSpace == isCurrCharWhiteSpace)
                {
                    continue;
                }

                if (isPrevCharWhiteSpace)
                {
                    wordCount++;
                }

                isPrevCharWhiteSpace = isCurrCharWhiteSpace;
                lastChar = currChar;
            }

            return new WordCountResult(firstChar, lastChar, wordCount);
        }
    }

    public static class WordCountResult
    {
        final char m_cFirstChar;
        final char m_cLastChar;
        final long m_lWordCount;

        public WordCountResult(char cFirstChar, char cLastChar, long lWordCount)
        {
            this.m_cFirstChar = cFirstChar;
            this.m_cLastChar = cLastChar;
            this.m_lWordCount = lWordCount;
        }
    }
}
