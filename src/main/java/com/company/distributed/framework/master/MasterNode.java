package com.company.distributed.framework.master;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MasterNode
{
    public static void main(String[] args) throws Exception
    {
        HeartbeatService hbService = new HeartbeatService(8081);

        hbService.startService();

        TimeUnit.HOURS.sleep(1);
    }

    private static class WorkerRegistry
    {
        private final Map<String, Worker> m_workers = new ConcurrentHashMap<>();

        void addWorker(Worker worker)
        {
            m_workers.put(worker.m_workerId, worker);
        }

        void removeWorker(String workerId)
        {
            m_workers.remove(workerId);
        }

        int size()
        {
            return m_workers.size();
        }
    }

    private static class Worker
    {
        private final String m_workerId;

        public Worker(String m_workerId)
        {
            this.m_workerId = m_workerId;
        }
    }
}
