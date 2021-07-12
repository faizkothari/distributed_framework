//package com.company.distributed.framework.master;
//
//import java.util.Map;
//
//public class MembershipService
//{
//    void join(String workerId)
//    {
//        /*
//
//        MemberJoinSet.add(workerId);
//
//         */
//    }
//
//    void onHeartbeat(String workerId, long workerHeartbeatMillis, long currTimeMillis)
//    {
//        if (currTimeMillis - workerHeartbeatMillis > 5)
//        {
//            // ignore the heartbeat
//            return;
//        }
//
//        if (!workerRegistry.contains(workerId))
//        {
//            // ignore the heartbeat
//            return;
//        }
//
//        workerRegistry.updateHeartbeat(workerId, currTimeMillis);
//    }
//
//    void remove(String workerId)
//    {
//        /*
//        MemberRemovalSet.add(workerId)
//         */
//    }
//
//    private static class MembershipLivelinessCheck implements Runnable
//    {
//
//        @Override
//        public void run()
//        {
//            long currTimeMillis = System.currentTimeMillis();
//            Map<String, Long> heartbeats = WorkerRegistry.getAllWorkerHeartbeats();
//
//            for (Map.Entry<String, Long> heartbeatEntry: heartbeats.entrySet())
//            {
//                if (currTimeMillis - heartbeatEntry.getValue() > 5)
//                {
//                    MembershipService.remove(heartbeatEntry.getKey());
//                }
//            }
//
//            // Trigger Reassignment
//        }
//    }
//}
