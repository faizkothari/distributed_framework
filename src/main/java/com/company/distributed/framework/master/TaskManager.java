package com.company.distributed.framework.master;

import java.util.Comparator;
import java.util.PriorityQueue;

public class TaskManager
{
//    private final int m_maxWorkerCount;
//    private final int m_maxChunkSize;

    private final PriorityQueue<Task> finishedTasks = new PriorityQueue<>(Comparator.comparingInt(t -> t.m_taskId));

    public TaskManager(int maxWorkerCount, int maxChunkSize)
    {
        maxWorkerCount = maxWorkerCount;
        maxChunkSize = maxChunkSize;
    }

    private synchronized void addToFinishedTask(Task task)
    {
        finishedTasks.add(task);
    }

    private static class Task
    {
        private final int m_taskId;
        private final long m_startOffset;
        private final long m_endOffset;
        private final int m_chunkSize;
        private final TaskManager m_tm;

        private long m_currOffset;
        private char m_firstChar = (char) -1;
        private char m_lastChar = m_firstChar;
        private long m_wordCount = 0;
        private boolean m_isPrevCharWhiteSpace = false;

        public Task(int taskId, long startOffset, long endOffset, int chunkSize, TaskManager tm)
        {
            m_taskId = taskId;
            m_startOffset = startOffset;
            m_endOffset = endOffset;
            m_chunkSize = chunkSize;
            m_tm = tm;

            m_currOffset = m_startOffset;
        }

        public synchronized long nextOffset()
        {
            return m_currOffset;
        }

        public synchronized void commitOffset(long offset, char firstChar, char lastChar, long wordCount)
        {
            if (offset != m_currOffset)
            {
                return;
            }

            boolean isCurrCharWhiteSpace = Character.isWhitespace(firstChar);

            if (m_isPrevCharWhiteSpace && !isCurrCharWhiteSpace)
            {
                wordCount++;
            }

            m_wordCount += wordCount;

            m_lastChar = lastChar;
            m_isPrevCharWhiteSpace = Character.isWhitespace(lastChar);

            if (m_firstChar == (char) -1)
            {
                m_firstChar = firstChar;
            }

            if (m_currOffset + m_chunkSize >= m_endOffset)
            {
                m_currOffset = -1;
                m_tm.addToFinishedTask(this);
            }
            else
            {
                m_currOffset += m_chunkSize;
            }
        }
    }
}
