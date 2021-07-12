package com.company.distributed.framework.commons;

import com.company.distributed.framework.worker.WorkerNode;

public class WordCountTask
{
    private final String m_code;
    private final String m_fileName;
    private final long m_startOffset;
    private final int m_chunkSize;

    public WordCountTask(String m_code, String m_fileName, long m_startOffset, int m_chunkSize)
    {
        this.m_code = m_code;
        this.m_fileName = m_fileName;
        this.m_startOffset = m_startOffset;
        this.m_chunkSize = m_chunkSize;
    }
}
